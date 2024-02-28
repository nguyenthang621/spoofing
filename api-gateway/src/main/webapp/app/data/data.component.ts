import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';
import { EntityService } from 'app/common/model/entity.service';
import { AccountService } from 'app/core/auth/account.service';
import { Title } from '@angular/platform-browser';
import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
// + Modal
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { plainToFlattenObject } from 'app/common/util/request-util';
import { createRequestOption } from 'app/shared/util/request-util';
// + search
import * as _ from 'lodash';
// + mobile friendly
import { DeviceDetectorService } from 'ngx-device-detector';
import { saveAs } from 'file-saver';

@Component({
  selector: 'jhi-data',
  templateUrl: './data.component.html'
})
export class DataComponent implements OnInit, OnDestroy {
  _ = _;
  isMobile: boolean;
  isReady = false;
  currentAccount: any;
  // + data
  tasks: any[] = []; // List of available task from this UI
  actions: any[] = []; // List of available options for each row
  fields: any;
  rows: any[] = [];
  columns: any[] = [];
  columnKeys: string[] = [];
  columnsMap: any = {};
  // How to display the table
  title = ''; // page title
  prop = ''; // entity namespace
  svc = ''; // service namespace
  apiEndpoint = '';
  queryParams: any;
  // + states
  error: any;
  success: any;
  eventSubscriber: any;
  // + pagination
  links: any;
  totalItems: any;
  itemsPerPage: any;
  page: any;
  predicate: any;
  previousPage: any;
  ascending: any;
  // + search support
  filterOperators: string[] = [];
  searchModel: any;
  searchParams: any;
  // + delete Modal
  deleteTitle = '';
  @ViewChild('deleteModal', { static: true }) deleteModal: any;
  // + references
  referenceMap: any = {};
  referenceEndpoint: any = {};

  constructor(
    private titleService: Title,
    private deviceService: DeviceDetectorService,
    private httpClient: HttpClient,
    private dataService: EntityService,
    private parseLinks: JhiParseLinks,
    private alertService: JhiAlertService,
    private accountService: AccountService,
    private activatedRoute: ActivatedRoute,
    private router: Router,
    private modalService: NgbModal,
    private eventManager: JhiEventManager
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
    this.isMobile = this.deviceService.isMobile();
  }

  loadAll(): void {
    // eslint-disable-next-line no-console
    console.log('Activated route', this.activatedRoute);
    this.dataService
      .query(
        _.assign(
          {},
          this.queryParams,
          {
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort()
          },
          // + support search
          plainToFlattenObject(
            _.pickBy(
              _.mapValues(this.searchParams, (pattern, field) =>
                this.searchModel[field] ? _.template(pattern)(_.assign({}, { term: this.searchModel[field] }, this.searchModel)) : null
              ),
              _.identity
            )
          )
        ),
        this.apiEndpoint
      )
      .subscribe(
        (res: HttpResponse<any[]>) => this.paginate(res.body || [], res.headers),
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  loadPage(page: number): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.transition();
    }
  }

  transition(): void {
    this.router.navigate([], {
      queryParams: _.assign(
        {},
        {
          page: this.page,
          size: this.itemsPerPage,
          sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc')
        },
        this.searchModel
      )
    });
    this.loadAll();
  }

  clear(): void {
    this.page = 1;
    this.searchModel = {};
    const uri = window.location.pathname;
    this.router.navigateByUrl('/').then(() =>
      this.router.navigate([uri], {
        queryParams: _.assign(
          {},
          {
            page: this.page,
            size: this.itemsPerPage,
            sort: this.predicate + ',' + (this.ascending ? 'asc' : 'desc')
          }
        )
      })
    );
  }

  ngOnInit(): void {
    this.isReady = false;
    combineLatest(
      this.activatedRoute.data.pipe(
        map(data => {
          // + pagination parameters
          this.page = data.pagingParams.page;
          this.previousPage = data.pagingParams.page;
          this.ascending = _.get(data.templateFile, 'config.ascending', data.pagingParams.ascending);
          this.predicate = _.get(data.templateFile, 'config.predicate', data.pagingParams.predicate);
          // + prop and yaml
          this.prop = data.templateFile.prop;
          this.svc = data.templateFile.svc;
          this.title = _.get(data.templateFile, 'config.title.index', 'app.title.' + this.prop);
          this.titleService.setTitle(this.title);
          this.deleteTitle = _.get(data.templateFile, 'config.title.delete', 'app.delete.' + this.prop);
          // + apiEndpoint and params
          this.tasks = _.get(data.templateFile, 'config.tasks', []);
          this.actions = _.get(data.templateFile, 'config.actions', []);
          this.apiEndpoint = _.get(data.templateFile, 'config.apiEndpoint', data.templateFile.apiEndpoint);
          this.queryParams = _.get(data.templateFile, 'config.queryParams', {});
          // + fields
          this.fields = _.get(data.templateFile, 'config.fields', []);
          this.columns = _.map(_.get(data.templateFile, 'config.columns', ['id']), v =>
            _.isString(v) ? { prop: v, pattern: 'ci(contains(${ term }))', jhiTranslate: v, label: v } : v
          );
          this.columnsMap = _.keyBy(this.columns, 'prop');
          this.columnKeys = _.map(this.columns, 'prop');
          // modifier for the search stuff
          this.searchParams = _.mapValues(_.keyBy(this.columns, 'prop'), v => v.pattern || '${term}');
          // TODO: Store the list of display columns under account preferences or session storage
          this.filterOperators = _.get(data.templateFile, 'config.filterOperators');
          // + calculate filtering map for select field, which annotated with `options`
          this.referenceMap = {};
          _.each(
            _.filter(this.columns, i => _.get(i, 'options')),
            i => _.each(i.options, o => _.set(this.referenceMap, [i.prop, o.value], o.label))
          );
          // + calculate reference for ng-select
          this.referenceEndpoint = {};
          _.each(
            _.filter(this.columns, i => _.get(i, 'apiEndpoint')),
            field => (this.referenceEndpoint[field.prop] = _.pick(field, ['apiEndpoint', 'params', 'key', 'val']))
          );
        })
      ),
      this.activatedRoute.queryParams.pipe(map(params => (this.searchModel = _.omit(params, ['size', 'sort', 'page']))))
    ).subscribe(() => this.loadAll());
    this.accountService.identity().subscribe(account => (this.currentAccount = account));
    this.registerChangeInData();
  }

  ngOnDestroy(): void {
    if (this.eventSubscriber) {
      this.eventManager.destroy(this.eventSubscriber);
    }
  }

  trackId(index: number, item: any): string {
    return item.id;
  }

  registerChangeInData(): void {
    this.eventSubscriber = this.eventManager.subscribe('dataListModification', () => this.loadAll());
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.ascending ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected paginate(data: any[], headers: HttpHeaders): void {
    this.links = this.parseLinks.parse(headers.get('link') || '');
    this.totalItems = parseInt(headers.get('X-Total-Count') || '0', 10);
    this.rows = data;
    this.loadReferences();
    this.isReady = true;
  }

  protected loadReferences(): void {
    // + load reference remote entities based on apiEndpoint
    _.each(this.referenceEndpoint, (templateOptions, fieldKey) => {
      const ids = _.uniq(
        _.flatMap(
          _.map(this.rows, i => _.get(i, fieldKey)).filter(i => !_.isEmpty(i)),
          values => (_.isArray(values) ? _.values(values) : values)
        )
      );
      const q = _.get(templateOptions, 'params', {});
      _.set(q, templateOptions.key, ids);
      this.dataService
        .query(q, templateOptions.apiEndpoint)
        .subscribe(refData =>
          _.each(refData.body, i => _.set(this.referenceMap, [fieldKey, _.get(i, templateOptions.key)], _.get(i, templateOptions.val)))
        );
    });
  }

  protected onError(errorMessage: string): void {
    this.alertService.error(errorMessage);
  }
  // + delete confirm
  delete(t: any): void {
    this.modalService.open(this.deleteModal).result.then(
      () =>
        this.dataService.delete(t.id, this.apiEndpoint).subscribe(
          () => this.loadAll(),
          err => this.onError(err.error.title)
        ),
      () => this.modalService.dismissAll()
    );
  }

  // Render cell value based on current reference map
  renderCell(row: any, col: string): any {
    // {{ _.get(referenceMap, [c, _.get(val, c)], _.get(val, c)) }}
    const val = this.columnsMap[col].template ? _.template(this.columnsMap[col].template)(row) : _.get(row, col);
    if (_.isArray(val)) {
      return _.map(val, v => _.get(this.referenceMap, [col, v], v));
    } else if (_.isPlainObject(val)) {
      return JSON.stringify(val);
    }
    return _.get(this.referenceMap, [col, val], val);
  }

  // + export data
  exportData(): void {
    this.dataService
      .query(
        _.assign(
          this.queryParams,
          {
            page: this.page - 1,
            size: 10000,
            sort: this.sort()
          },
          this.searchModel
        ),
        this.apiEndpoint
      )
      .subscribe(res => this.saveDataToFile(res.body || []));
  }
  // + save array of json to file
  saveDataToFile(data: any[]): void {
    const blob = new Blob([JSON.stringify(data)], {
      type: 'application/json'
    });
    saveAs(blob, this.prop + '.json');
  }

  // + perform task and action
  performTask(task: any): void {
    if (task.url) {
      this.router.navigateByUrl(task.url);
    } else if (task.apiEndpoint) {
      this.httpClient
        .request(task.method || 'GET', task.apiEndpoint, {
          params: task.params,
          body: task.body,
          observe: 'response',
          responseType: 'json'
        })
        .pipe(
          filter((res: HttpResponse<any>) => res.ok),
          map((res: HttpResponse<any>) => res.body || {})
        )
        .subscribe(
          () => this.alertService.success(task.successMsg || 'Successfully perform task'),
          () => this.alertService.error(task.errorMsg || 'Failed to perform task')
        );
    } else if (task.fileUrl) {
      this.httpClient
        .request(task.method || 'GET', task.fileUrl, {
          params: task.params,
          body: task.body,
          observe: 'response',
          responseType: 'blob'
        })
        .pipe(
          filter((res: HttpResponse<any>) => res.ok),
          map((res: HttpResponse<any>) => res.body || {})
        )
        .subscribe(
          blob => saveAs(blob, task.fileName || 'download'),
          () => this.alertService.error(task.errorMsg || 'Failed to download file')
        );
    }
  }

  // + perform action
  performAction(action: any, row: any): void {
    if (action.url) {
      this.router.navigateByUrl(_.template(action.url)(row));
    } else if (action.apiEndpoint) {
      const apiEndpoint = _.template(action.apiEndpoint)(row);
      const params = action.params ? createRequestOption(JSON.parse(_.template(JSON.stringify(action.params))(row))) : undefined;
      const body = action.body ? JSON.parse(_.template(JSON.stringify(action.body))(row)) : undefined;
      this.httpClient
        .request(action.method || 'GET', apiEndpoint, {
          params,
          body,
          observe: 'response',
          responseType: 'json'
        })
        .pipe(
          filter((res: HttpResponse<any>) => res.ok),
          map((res: HttpResponse<any>) => res.body || {})
        )
        .subscribe(
          () => this.alertService.success(action.successMsg || 'Successfully perform task'),
          () => this.alertService.error(action.errorMsg || 'Failed to perform task')
        );
    } else if (action.fileUrl) {
      const params = action.params ? createRequestOption(JSON.parse(_.template(JSON.stringify(action.params))(row))) : undefined;
      const body = action.body ? JSON.parse(_.template(JSON.stringify(action.body))(row)) : undefined;
      this.httpClient
        .request(action.method || 'GET', action.fileUrl, {
          params,
          body,
          observe: 'response',
          responseType: 'blob'
        })
        .pipe(
          filter((res: HttpResponse<any>) => res.ok),
          map((res: HttpResponse<any>) => res.body || {})
        )
        .subscribe(
          blob => saveAs(blob, action.fileName || 'download'),
          () => this.alertService.error(action.errorMsg || 'Failed to download file')
        );
    }
  }
}
