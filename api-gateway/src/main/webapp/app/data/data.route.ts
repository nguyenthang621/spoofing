import { Injectable } from '@angular/core';
import { HttpResponse, HttpClient } from '@angular/common/http';
import { Resolve, ActivatedRouteSnapshot, Routes } from '@angular/router';
import { JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';
import { Observable, of } from 'rxjs';
import { filter, map, concatMap } from 'rxjs/operators';
// + look for anything
import * as _ from 'lodash';
import * as jsyaml from 'js-yaml';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { DataComponent } from './data.component';
import { DataDetailComponent } from './data-detail.component';
import { DataUpdateComponent } from './data-update.component';
import { DataImportComponent } from './data-import.component';

@Injectable({ providedIn: 'root' })
export class TemplateFilenameResolve implements Resolve<string> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const prop = _.get(route.params, 'prop');
    const svc = _.get(route.params, 'svc');
    return this.service.get(`assets/${svc}/${prop}.yaml?time=` + new Date().getTime(), { responseType: 'text', observe: 'response' }).pipe(
      filter(response => response.ok),
      map(response => jsyaml.load(response.body || '')),
      map(config => ({ prop, svc, config, apiEndpoint: `${svc ? 'services/' + svc : ''}/api/${prop}` }))
    );
  }
}

@Injectable({ providedIn: 'root' })
export class DataResolve implements Resolve<any> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const id = _.get(route.params, 'id');
    const prop = _.get(route.params, 'prop');
    const svc = _.get(route.params, 'svc');
    if (id) {
      return this.service
        .get(`assets/${svc}/${prop}.yaml?time=` + new Date().getTime(), { responseType: 'text', observe: 'response' })
        .pipe(
          filter(response => response.ok),
          map(notification => jsyaml.load(notification.body || '')),
          concatMap(jsconfig =>
            this.service
              .get(SERVER_API_URL + _.get(jsconfig, 'apiEndpoint', `${svc ? 'services/' + svc : ''}/api/${prop}`) + `/${id}`, {
                params: createRequestOption(_.get(jsconfig, 'queryParams', {})),
                observe: 'response'
              })
              .pipe(
                filter((response: HttpResponse<any>) => response.ok),
                map((data: HttpResponse<any>) => data.body)
              )
          )
        );
    }
    return of({});
  }
}

export const dataRoute: Routes = [
  {
    path: ':svc/:prop',
    component: DataComponent,
    resolve: {
      templateFile: TemplateFilenameResolve,
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: 'id,asc',
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':svc/:prop/:id/view',
    component: DataDetailComponent,
    resolve: {
      templateFile: TemplateFilenameResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':svc/:prop/import',
    component: DataImportComponent,
    resolve: {
      templateFile: TemplateFilenameResolve
    },
    data: {
      authorities: ['ROLE_ADMIN'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':svc/:prop/new',
    component: DataUpdateComponent,
    resolve: {
      templateFile: TemplateFilenameResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':svc/:prop/:id/edit',
    component: DataUpdateComponent,
    resolve: {
      templateFile: TemplateFilenameResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: ':svc/:prop/:id/copy',
    component: DataUpdateComponent,
    resolve: {
      templateFile: TemplateFilenameResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title',
      copy: true
    },
    canActivate: [UserRouteAccessService]
  }
];
