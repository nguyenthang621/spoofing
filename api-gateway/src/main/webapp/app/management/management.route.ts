import { Injectable } from '@angular/core';

import { Resolve, ActivatedRouteSnapshot, Routes } from '@angular/router';
import { UserRouteAccessService } from 'app/core/auth/user-route-access-service';

// + rendering markdown pages
import { HttpResponse, HttpClient } from '@angular/common/http';
import { SERVER_API_URL, BUILD_TIMESTAMP } from 'app/app.constants';

import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as jsyaml from 'js-yaml';

// + module components
import { FormComponent } from './form/form.component';
import { SettingsComponent } from './settings/settings.component';
import { ConfigurationComponent } from './configuration/configuration.component';

// + resolve yaml files from query param
@Injectable({ providedIn: 'root' })
export class YamlFileResolve implements Resolve<string> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    return this.service
      .get(
        `${SERVER_API_URL}assets/${(route.params.path ? route.params.path + '/' : 'config/') + route.params.doc}.yml?ts=${BUILD_TIMESTAMP}`,
        { responseType: 'text', observe: 'response' }
      )
      .pipe(
        filter((response: HttpResponse<string>) => response.ok),
        map((content: HttpResponse<string>) => jsyaml.load(content.body || ''))
      );
  }
}

// + management module routes
export const MANAGEMENT_ROUTES: Routes = [
  // Settings
  {
    path: 'settings/:doc',
    component: SettingsComponent,
    resolve: {
      settings: YamlFileResolve
    },
    data: {
      authorities: ['ROLE_ADMIN'],
      pageTitle: 'management.title'
    },
    canActivate: [UserRouteAccessService]
  },
  // Optional Settings
  {
    path: 'configuration/:doc',
    component: ConfigurationComponent,
    data: {
      authorities: ['ROLE_ADMIN'],
      pageTitle: 'management.title'
    },
    canActivate: [UserRouteAccessService]
  },
  // Other custom form
  {
    path: 'forms/:path/:doc',
    component: FormComponent,
    resolve: {
      settings: YamlFileResolve
    },
    data: {
      authorities: ['ROLE_ADMIN'],
      pageTitle: 'management.title'
    },
    canActivate: [UserRouteAccessService]
  }
];
