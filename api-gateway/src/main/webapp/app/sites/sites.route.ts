import { Injectable } from '@angular/core';

import { Resolve, ActivatedRouteSnapshot, Routes } from '@angular/router';

import { HttpResponse, HttpClient } from '@angular/common/http';

import { JhiResolvePagingParams } from 'ng-jhipster';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';

import { DashboardComponent } from './dashboard/dashboard.component';

import { DataComponent } from './data/data.component';
import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataUpdateComponent } from './data-update/data-update.component';
import { DataImportComponent } from './data-import/data-import.component';

import { SiteComponent } from './site/site.component';
import { Observable, of } from 'rxjs';
import { filter, map, concatMap } from 'rxjs/operators';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import * as _ from 'lodash';

// + resolve markdown documentation
@Injectable({ providedIn: 'root' })
export class SiteResolver implements Resolve<any> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    return this.service
      .get(SERVER_API_URL + 'api/nodes', { params: createRequestOption({ slug: route.params['site'], type: 'SITE' }), observe: 'response' })
      .pipe(
        filter((response: HttpResponse<any>) => response.ok),
        map((content: HttpResponse<any>) => content.body || []),
        filter(res => res.length > 0),
        map(res => res[0])
      );
  }
}
@Injectable({ providedIn: 'root' })
export class ContentTypeResolve implements Resolve<any> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    // + retrieve sites first
    return this.service
      .get(SERVER_API_URL + 'api/nodes', { params: createRequestOption({ slug: route.params.site, type: 'SITE' }), observe: 'response' })
      .pipe(
        filter((response: HttpResponse<any>) => response.ok),
        map((content: HttpResponse<any>) => content.body || []),
        filter(res => res.length > 0),
        map(res => res[0]),
        // retrieve content types of this site
        concatMap(site =>
          this.service
            .get(SERVER_API_URL + 'api/nodes', {
              params: createRequestOption({ slug: route.params['type'], type: 'CONTENT_TYPE', tags: site.id }),
              observe: 'response'
            })
            .pipe(
              filter((response: HttpResponse<any>) => response.ok),
              map((content: HttpResponse<any>) => content.body || []),
              filter(res => res.length > 0),
              map(res => _.set(res[0], 'site', site)),
              concatMap(contentType =>
                this.service
                  .get(SERVER_API_URL + 'api/nodes', {
                    params: createRequestOption({ tags: contentType.id, type: 'FIELD' }),
                    observe: 'response'
                  })
                  .pipe(
                    filter((response: HttpResponse<any>) => response.ok),
                    map((content: HttpResponse<any>) => _.set(contentType, 'fields', content.body || []))
                  )
              )
            )
        )
      );
  }
}

@Injectable({ providedIn: 'root' })
export class DataResolve implements Resolve<any> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    if (route.params.id) {
      return this.service.get(SERVER_API_URL + 'api/nodes/' + route.params.id, { observe: 'response' }).pipe(
        filter(response => response.ok),
        map(notification => notification.body)
      );
    }
    return of({});
  }
}
// + home module routes
export const SITE_ROUTES: Routes = [
  // + landing pages with all the sites
  {
    path: '',
    component: DashboardComponent,
    data: {
      authorities: [],
      pageTitle: 'home.title'
    }
  },
  // + specific site layout
  {
    path: 'site/:site',
    component: SiteComponent,
    resolve: {
      site: SiteResolver
    },
    data: { pageTitle: 'home.title' }
  },
  // + specific data for one site
  {
    path: 'site/:site/contents/:type',
    component: DataComponent,
    resolve: {
      contentType: ContentTypeResolve,
      pagingParams: JhiResolvePagingParams
    },
    data: {
      authorities: ['ROLE_USER'],
      defaultSort: '_id,asc',
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'site/:site/contents/:type/:id/view',
    component: DataDetailComponent,
    resolve: {
      contentType: ContentTypeResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'site/:site/contents/:type/import',
    component: DataImportComponent,
    resolve: {
      contentType: ContentTypeResolve
    },
    data: {
      authorities: ['ROLE_ADMIN'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'site/:site/contents/:type/new',
    component: DataUpdateComponent,
    resolve: {
      contentType: ContentTypeResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'site/:site/contents/:type/:id/edit',
    component: DataUpdateComponent,
    resolve: {
      contentType: ContentTypeResolve,
      model: DataResolve
    },
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'data.title'
    },
    canActivate: [UserRouteAccessService]
  }
];
