import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse, HttpResponse, HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { filter, map, catchError } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { EntityService } from 'app/common/model/entity.service';
import { createRequestOption } from 'app/shared/util/request-util';
import { AccountService } from 'app/core/auth/account.service';
import { SERVER_API_URL, DEBUG_INFO_ENABLED } from 'app/app.constants';
// + search
import * as _ from 'lodash';
import * as jsyaml from 'js-yaml';

@Component({
  selector: 'jhi-admin-settings',
  templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {
  currentAccount: any;
  // + config
  fields: any = {};
  settings: any;
  columns: string[] = [];
  displayColumns: any = {};
  prop = '';
  svc = '';
  apiEndpoint = '';
  _ = _;
  searchModel: any = {};
  debug = DEBUG_INFO_ENABLED;
  error: any = {};
  success: any = {};

  constructor(
    protected entityService: EntityService,
    protected httpClient: HttpClient,
    protected jhiAlertService: JhiAlertService,
    protected accountService: AccountService,
    protected activatedRoute: ActivatedRoute,
    protected router: Router
  ) {
    this.settings = {};
  }

  loadAll(): void {
    this.httpClient
      .get<any[]>(SERVER_API_URL + 'api/consul/configs', { observe: 'response' })
      .subscribe(
        (res: HttpResponse<any[]>) => this.loadDetails(res.body || []),
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit(): void {
    this.loadAll();
    this.accountService.identity().subscribe(account => (this.currentAccount = account));
  }

  protected onError(errorMessage: string): void {
    this.jhiAlertService.error(errorMessage);
  }

  // + load all configuration
  loadDetails(config: string[]): void {
    const keys = config.map(i => i.split('/')[1]);
    // this.fields = _.zipObject(keys, keys.map(i => []));
    this.loadValues(keys);
    this.loadForms(keys);
  }

  // + load value for a specific key
  loadValues(keys: string[]): void {
    forkJoin(
      _.zipObject(
        keys,
        keys.map(key =>
          this.httpClient
            .get(SERVER_API_URL + 'api/consul/config', {
              params: createRequestOption({ key: `config/${key}/data` }),
              observe: 'response',
              responseType: 'text'
            })
            .pipe(
              filter(res => res.ok),
              map(res => jsyaml.load(res.body || ''))
            )
        )
      )
    ).subscribe(
      success => (this.settings = success),
      err => console.error(err)
    );
  }

  loadForms(keys: string[]): void {
    forkJoin(
      _.zipObject(
        keys,
        keys.map(key =>
          this.httpClient
            .get(SERVER_API_URL + `assets/management/${key}.yaml`, {
              params: createRequestOption({ ts: new Date().getTime() }),
              observe: 'response',
              responseType: 'text'
            })
            .pipe(
              map(res => jsyaml.load(res.body || '')),
              catchError(() => of({}))
            )
        )
      )
    ).subscribe(
      success => (this.fields = _.omitBy(success, _.isEmpty)),
      err => console.error(err)
    );
  }

  save(key: string): void {
    this.httpClient
      .post(SERVER_API_URL + 'management/settings', jsyaml.dump(this.settings[key]), {
        params: createRequestOption({ key: `config/${key}/data` }),
        observe: 'response',
        responseType: 'text'
      })
      .subscribe();
  }
  refresh(): void {
    this.httpClient.post(SERVER_API_URL + 'management/refresh', { observe: 'response' }).subscribe();
  }
}
