import { Component, OnInit, OnDestroy } from '@angular/core';
// + HTTP support
import { HttpClient, HttpResponse } from '@angular/common/http';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { plainToFlattenObject } from 'app/common/util/request-util';
// + ng-select
import { Subject, of, concat } from 'rxjs';
import { FieldType } from '@ngx-formly/core';
import { distinctUntilChanged, filter, debounceTime, switchMap, tap, catchError, map } from 'rxjs/operators';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-formly-field-ngselect',
  template: `
    <ng-select
      *ngIf="to.items || to.itemEndpoint"
      [items]="to.items"
      [placeholder]="to.placeholder"
      [multiple]="to.multiple"
      [hideSelected]="to.hideSelected"
      [bindLabel]="to.val"
      [bindValue]="to.key"
      [groupBy]="to.groupBy"
      [selectableGroup]="to.selectableGroup"
      [selectableGroupAsModel]="to.selectableGroupAsModel"
      [formControl]="formControl"
    >
    </ng-select>
    <ng-select
      *ngIf="!to.items && to.termPattern !== false"
      [items]="options$ | async"
      [placeholder]="to.placeholder"
      [typeahead]="search$"
      [multiple]="to.multiple"
      [hideSelected]="to.hideSelected"
      [loading]="isLoading"
      [bindLabel]="to.val"
      [bindValue]="to.key"
      [groupBy]="to.groupBy"
      [formControl]="formControl"
    >
    </ng-select>
    <ng-select
      *ngIf="!to.items && to.termPattern === false"
      [items]="options$ | async"
      [placeholder]="to.placeholder"
      [typeahead]="search$"
      [multiple]="to.multiple"
      (focus)="search$.next()"
      [hideSelected]="to.hideSelected"
      [loading]="isLoading"
      [bindLabel]="to.val"
      [bindValue]="to.key"
      [groupBy]="to.groupBy"
      [formControl]="formControl"
    >
    </ng-select>
  `
})
export class NgselectTypeComponent extends FieldType implements OnInit, OnDestroy {
  defaultOptions = {
    wrappers: ['form-field']
  };
  onDestroy$ = new Subject<void>();
  options$: any;
  isLoading = false;
  search$ = new Subject<string>();

  constructor(public httpClient: HttpClient) {
    super();
  }

  ngOnInit(): void {
    // FIXME: Load existings value from formControl to populate into of
    // Support TypeAhed
    if (this.to.itemEndpoint) {
      this.httpClient.get<any[]>(this.to.itemEndpoint).subscribe(res => (this.to.items = res));
    } else if (!this.to.items) {
      this.options$ = concat(
        // default items from templateOptions,
        this.formControl.value
          ? // Try to retrieve all the keyed values
            this.loadSelected()
          : of(_.get(this.to, 'defaultValue', [])),
        this.search$.pipe(
          debounceTime(200),
          distinctUntilChanged(),
          tap(() => (this.isLoading = true)),
          filter((term: string) =>
            this.to.termLength ? term.length >= this.to.termLength : this.to.termPattern === false || !_.isEmpty(term)
          ),
          switchMap((term: string) =>
            this.loadSeachResult(term).pipe(
              filter((res: HttpResponse<any[]>) => res.ok),
              map((res: HttpResponse<any[]>) => this.parseResult(res.body || [])), // The original array
              catchError(() => of([])), // empty list on error
              tap(() => (this.isLoading = false))
            )
          )
        )
      );
      this.options$.subscribe();
    }
  }

  ngOnDestroy(): void {
    this.onDestroy$.complete();
  }

  loadSelected(): any {
    return this.httpClient
      .get<any>(SERVER_API_URL + this.to.apiEndpoint, {
        params: createRequestOption(
          _.omitBy(plainToFlattenObject(_.assign({}, this.to.params, _.set({}, this.to.key, this.formControl.value))), _.isNull)
        ),
        observe: 'response'
      })
      .pipe(
        map(res => this.parseResult(res.body)), // The original array
        catchError(() => of([])), // empty list on error
        tap(() => (this.isLoading = false))
      );
  }

  loadSeachResult(term: string): any {
    const query = _.assign({}, this.to.params);
    if (this.to.termPattern) {
      _.set(query, this.to.val, this.to.termPattern ? this.to.termPattern.replace('${term}', term) : term);
    }
    const body = _.assign({}, this.to.body);
    if (!_.isEmpty(body) && this.to.termPattern) {
      _.set(query, this.to.val, this.to.termPattern ? this.to.termPattern.replace('${term}', term) : term);
    }
    if (this.to.sendPost || !_.isEmpty(body)) {
      return this.httpClient.post<any>(SERVER_API_URL + this.to.apiEndpoint, _.omitBy(plainToFlattenObject(body), _.isNull), {
        params: createRequestOption(_.omitBy(plainToFlattenObject(query), _.isNull)),
        observe: 'response'
      });
    } else {
      return this.httpClient.get<HttpResponse<any[]>>(SERVER_API_URL + this.to.apiEndpoint, {
        params: createRequestOption(_.omitBy(plainToFlattenObject(query), _.isNull)),
        observe: 'response'
      });
    }
  }

  parseResult(res: any[]): any {
    if (this.to.key) {
      res = _.map(res, item => plainToFlattenObject(item)); // Convert nested one into path
      res = _.map(res, obj => _.mapKeys(obj, (v, k) => _.replace(k, '.', '$')));
      res = _.uniqBy(res, this.to.key); // extract the uniq part
      res = _.filter(res, i => _.get(i, this.to.key)); // Filter out null key
    }
    return res;
  }
}
