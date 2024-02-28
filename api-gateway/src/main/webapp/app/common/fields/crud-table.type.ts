import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
// + HTTP support
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { plainToFlattenObject } from 'app/common/util/request-util';
// + ng-select
import { filter } from 'rxjs/operators';
import { FieldArrayType } from '@ngx-formly/core';
import * as _ from 'lodash';
import { JhiParseLinks, JhiAlertService } from 'ng-jhipster';
// + Modal
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { EntityService } from 'app/common/model/entity.service';

@Component({
  selector: 'jhi-formly-crud-table',
  templateUrl: './crud-table.type.html'
})
export class CrudTableTypeComponent extends FieldArrayType implements OnInit, OnDestroy {
  rows: any[] = [];
  columns: any[] = [];
  columnKeys: string[] = [];
  columnNames: string[] = [];
  prop = '';
  apiEndpoint = '';
  _ = _;
  // pagination
  links = '';
  totalItems = 0;
  itemsPerPage = 20;
  page = 1;
  predicate: any;
  previousPage: any;
  reverse: any;
  // + selected
  hideSelected = true;
  // + delete Modal
  @ViewChild('deleteModal', { static: true }) deleteModal: any;
  @ViewChild('formModal', { static: true }) formModal: any;
  rowIdx = 0;
  modalModel: any = {};
  // + references
  referenceMap: any = {};
  referenceEndpoint: any = {};

  constructor(
    private httpClient: HttpClient,
    private jhiAlertService: JhiAlertService,
    protected parseLinks: JhiParseLinks,
    protected modalService: NgbModal,
    protected dataService: EntityService
  ) {
    super();
  }

  ngOnInit(): void {
    this.page = 1;
    this.itemsPerPage = _.get(this.to, 'itemsPerPage', 1000);
    this.predicate = _.get(this.to, 'predicate', 'id');
    this.reverse = _.get(this.to, 'reverse', true);
    this.hideSelected = _.get(this.to, 'hideSelected', true);
    this.columns = _.get(this.to, 'columns', ['id']);
    this.columnKeys = _.map(this.columns, c => _.get(c, 'prop', c));
    this.columnNames = _.map(this.columns, c => _.get(c, 'label', c));
    // // + populate references
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
    this.loadAll();
  }

  loadAll(): void {
    this.httpClient
      .get<any[]>(SERVER_API_URL + this.to.apiEndpoint, {
        params: createRequestOption(
          _.assign(
            {},
            {
              page: this.page - 1,
              size: this.itemsPerPage,
              sort: this.sort()
            },
            plainToFlattenObject(this.to.params)
          )
        ),
        observe: 'response'
      })
      .pipe(filter(res => res.ok))
      .subscribe(
        res => this.paginate(res.body || [], res.headers),
        err => this.onError(err.message)
      );
  }

  loadPage(page: number): void {
    if (page !== this.previousPage) {
      this.previousPage = page;
      this.loadAll();
    }
  }

  ngOnDestroy(): void {
    // this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: any): string {
    return item.id;
  }

  sort(): string[] {
    const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
    if (this.predicate !== 'id') {
      result.push('id');
    }
    return result;
  }

  // Paginate the data into table
  protected paginate(data: any[], headers: HttpHeaders): void {
    this.links = headers.get('link') ? this.parseLinks.parse(headers.get('link') || '') : null;
    this.totalItems = parseInt(headers.get('X-Total-Count') || '0', 10);
    this.rows = data;
    this.loadReferences();
  }

  loadReferences(): void {
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
    this.jhiAlertService.error(errorMessage);
  }

  // Update item
  editItem(model: any): void {
    this.modalModel = model;
    this.openUpdateModal();
  }
  // + delete confirm
  delete(model: any): void {
    this.modalModel = model;
    this.modalService.open(this.deleteModal).result.then(
      () => this.dataService.delete(model.id, SERVER_API_URL + this.to.apiEndpoint).subscribe(() => this.loadAll()),
      () => this.modalService.dismissAll()
    );
  }
  // Create
  create(): void {
    this.modalModel = {};
    _.each(this.to.params, (v, k) => _.set(this.modalModel, k, v));
    this.openUpdateModal();
  }
  // Open the modal
  openUpdateModal(): void {
    this.modalService.open(this.formModal, { size: 'lg' }).result.then(
      () =>
        (this.modalModel.id
          ? this.dataService.update(this.modalModel, SERVER_API_URL + this.to.apiEndpoint)
          : this.dataService.create(this.modalModel, SERVER_API_URL + this.to.apiEndpoint)
        ).subscribe(() => this.loadAll()),
      () => this.modalService.dismissAll()
    );
  }

  renderCell(row: any, col: any): any {
    // {{ _.get(referenceMap, [c, _.get(val, c)], _.get(val, c)) }}
    const val = _.get(row, col);
    if (_.isArray(val)) {
      return _.map(val, v => _.get(this.referenceMap, [col, v], v));
    } else if (_.isPlainObject(val)) {
      return JSON.stringify(val);
    }
    return _.get(this.referenceMap, [col, val], val);
  }
}
