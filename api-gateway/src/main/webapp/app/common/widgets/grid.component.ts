import { Component, OnInit, OnDestroy, ViewChild, Input } from '@angular/core';
import { HttpErrorResponse, HttpHeaders, HttpResponse, HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';
import { EntityService } from 'app/common/model/entity.service';
import { createRequestOption } from 'app/shared/util/request-util';
import { AccountService } from 'app/core/auth/account.service';
import { ITEMS_PER_PAGE } from 'app/shared/constants/pagination.constants';
// + Modal
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';

// + search
import * as _ from 'lodash';
import * as jsyaml from 'js-yaml';

// <jhi-grid src="/assets/path/entity.yaml" prop="" svc=""></jhi-grid>

@Component({
  selector: 'jhi-grid',
  templateUrl: './grid.component.html'
})
export class GridWidgetComponent implements OnInit, OnDestroy {
  // Abs path for the YAML
  @Input() src = '';
  _ = _;
  isReady = false;
  currentAccount: any;
  // + data
  fields: any;
  rows: any[] = [];
  columns: any[] = [];
  columnKeys: string[] = [];
  columnNames: string[] = [];
  displayColumns: any = {};
  @Input() prop = '';
  @Input() svc = '';
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
  reverse: any;
  // + search support
  filterOperators: string[] = [];
  searchModel: any;
  // + delete Modal
  @ViewChild('deleteModal', { static: true }) deleteModal: any;
  // + references
  referenceMap: any = {};
  referenceEndpoint: any = {};

  constructor(
    private httpClient: HttpClient,
    protected dataService: EntityService,
    protected parseLinks: JhiParseLinks,
    protected jhiAlertService: JhiAlertService,
    protected accountService: AccountService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router,
    protected modalService: NgbModal,
    protected eventManager: JhiEventManager
  ) {
    this.itemsPerPage = ITEMS_PER_PAGE;
  }

  loadAll(): void {
    this.dataService
      .query(
        _.assign(
          this.queryParams,
          {
            page: this.page - 1,
            size: this.itemsPerPage,
            sort: this.sort()
          },
          this.searchModel
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
          sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
        },
        this.searchModel
      )
    });
    this.loadAll();
  }

  clear(): void {
    this.page = 0;
    this.router.navigate([
      '',
      {
        page: this.page,
        sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
      }
    ]);
    this.loadAll();
  }

  ngOnInit(): void {
    this.isReady = false;
    combineLatest(this.resolvePagingParams(), this.resolveSrcFile(), this.resolveSearchModel()).subscribe(() => this.loadAll());
    this.accountService.identity().subscribe(account => (this.currentAccount = account));
    this.registerChangeInData();
  }

  resolveSearchModel(): any {
    return this.activatedRoute.queryParams.pipe(map(params => (this.searchModel = _.omit(params, ['size', 'sort', 'page']))));
  }
  resolvePagingParams(): any {
    return this.activatedRoute.queryParams.pipe(
      map(data => {
        this.page = _.get(data, 'page', 1);
        this.previousPage = _.get(data, 'page', 1);
        this.reverse = _.get(data, 'sort', 'id,desc');
        this.predicate = _.get(data, 'sort', 'desc');
      })
    );
  }
  resolveSrcFile(): any {
    return this.httpClient
      .get(this.src, { params: createRequestOption({ ts: new Date().getTime() }), responseType: 'text', observe: 'response' })
      .pipe(
        map(res => jsyaml.load(res.body || '')),
        map(data => _.set({}, 'templateFile.config', data)),
        map(data => {
          // + prop and yaml
          // this.prop = data.templateFile.prop;
          // this.svc = data.templateFile.svc;
          // + apiEndpoint and params
          this.apiEndpoint = _.get(data, 'templateFile.config.apiEndpoint');
          this.queryParams = _.get(data, 'templateFile.config.queryParams', {});
          // + fields
          this.fields = _.get(data, 'templateFile.config.fields', []);
          this.columns = _.get(data, 'templateFile.config.columns', ['id']);
          this.columnKeys = _.map(this.columns, c => _.get(c, 'prop', c));
          this.columnNames = _.map(this.columns, c => _.get(c, 'label', c));
          // TODO: Store the list of display columns under account preferences or session storage
          _.each(this.columnKeys, c => _.set(this.displayColumns, c, true));
          this.filterOperators = _.get(data, 'templateFile.config.filterOperators');
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
            field => (this.referenceEndpoint[field.prop] = _.pick(field, ['apiEndpoint', 'key', 'val']))
          );
        })
      );
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
    const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  protected paginate(data: any[], headers: HttpHeaders): void {
    if (headers.get('link')) {
      this.links = this.parseLinks.parse(headers.get('link') || '');
      this.totalItems = parseInt(headers.get('X-Total-Count') || '0', 10);
    }
    this.rows = data;
    // + load reference remote entities based on apiEndpoint
    _.each(this.referenceEndpoint, (templateOptions, fieldKey) => {
      const ids = _.uniq(
        _.flatMap(
          _.map(this.rows, i => _.get(i, fieldKey)).filter(i => !_.isEmpty(i)),
          values => (_.isArray(values) ? _.values(values) : values)
        )
      );
      const q = {};
      _.set(q, templateOptions.key, ids);
      this.dataService
        .query(q, templateOptions.apiEndpoint)
        .subscribe(refData =>
          _.each(refData.body, i => _.set(this.referenceMap, [fieldKey, _.get(i, templateOptions.key)], _.get(i, templateOptions.val)))
        );
    });
    this.isReady = true;
  }

  protected onError(errorMessage: string): void {
    this.jhiAlertService.error(errorMessage);
  }

  // Add search modifier
  setSearchOperator(field: string, operator: string): void {
    _.set(this.searchModel, field, `${operator}(${_.get(this.searchModel, field)})`);
  }
  toggleView(column: string): void {
    this.displayColumns[column] = !this.displayColumns[column];
  }
  // + delete confirm
  delete(t: any): void {
    this.modalService.open(this.deleteModal).result.then(
      () => this.dataService.delete(t.id, this.apiEndpoint).subscribe(() => this.loadAll()),
      () => this.modalService.dismissAll()
    );
  }
  navigate(link: any[]): void {
    this.router.navigate(link);
  }
}
