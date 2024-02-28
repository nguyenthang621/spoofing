import { Injectable } from '@angular/core';

import { Resolve, ActivatedRouteSnapshot, Routes } from '@angular/router';

import { HomeComponent } from './home.component';
import { TracingComponent } from './tracing/tracing.component';
import { SummaryComponent } from './summary/summary.component';
// + rendering markdown pages
import { HttpResponse, HttpClient } from '@angular/common/http';
import { BUILD_TIMESTAMP } from 'app/app.constants';

import { DocsComponent } from './docs/docs.component';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

// + resolve markdown documentation
@Injectable({ providedIn: 'root' })
export class MarkdownFileResolver implements Resolve<string> {
  constructor(private service: HttpClient) {}

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    return this.service
      .get(`assets/docs/${route.params.doc || 'index'}.md?ts=${BUILD_TIMESTAMP}`, { responseType: 'text', observe: 'response' })
      .pipe(
        filter((response: HttpResponse<string>) => response.ok),
        map((content: HttpResponse<string>) => content.body)
      );
  }
}

// + home module routes
export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: HomeComponent,
    data: {
      authorities: [],
      pageTitle: 'home.title'
    }
  },
  // Static pages
  {
    path: 'docs/:doc',
    component: DocsComponent,
    resolve: {
      markdown: MarkdownFileResolver
    },
    data: { pageTitle: 'home.title' }
  },
  {
    path: 'tracing',
    component: TracingComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'home.title'
    }
  },
  {
    path: 'summary',
    component: SummaryComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'home.title',
      apiEndpoint: '/services/spoofing/api/daily-stats'
    }
  },
  {
    path: 'summary-hcm',
    component: SummaryComponent,
    data: {
      authorities: ['ROLE_USER'],
      pageTitle: 'home.title',
      apiEndpoint: '/services/spoofing-hcm/api/daily-stats'
    }
  }
];
