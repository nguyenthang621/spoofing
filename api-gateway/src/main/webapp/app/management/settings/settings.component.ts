import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { filter, map } from 'rxjs/operators';
import { JhiAlertService } from 'ng-jhipster';
import { EntityService } from 'app/common/model/entity.service';
import { createRequestOption } from 'app/shared/util/request-util';
import { Title } from '@angular/platform-browser';
import { SERVER_API_URL } from 'app/app.constants';
import { FormGroup } from '@angular/forms';
// + search
import * as _ from 'lodash';
import * as jsyaml from 'js-yaml';

@Component({
  selector: 'jhi-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss']
})
export class SettingsComponent implements OnInit {
  _ = _;
  title = '';
  // + config
  fields: any[] = [];
  settings: any = {};
  model: any = {};
  apiEndpoint = '';
  queryParams: any = {};
  form = new FormGroup({});
  items: any[] = [];

  error: any;
  success: any;

  constructor(
    private entityService: EntityService,
    private titleService: Title,
    private httpClient: HttpClient,
    private jhiAlertService: JhiAlertService,
    private activatedRoute: ActivatedRoute
  ) {
    this.settings = {};
  }

  loadAll(): void {
    this.httpClient
      .get(SERVER_API_URL + this.apiEndpoint, { params: createRequestOption(this.queryParams), observe: 'response', responseType: 'text' })
      .pipe(
        filter(res => res.ok),
        map(res => jsyaml.load(res.body || ''))
      )
      .subscribe(
        res => (this.model = res),
        (err: HttpErrorResponse) => this.onError(err.message)
      );
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ settings }) => this.initialize(settings));
  }
  previousState(): void {
    window.history.back();
  }

  protected onError(errorMessage: string): void {
    this.jhiAlertService.error(errorMessage);
  }

  // Populate data from settings
  initialize(data: any): void {
    this.fields = data.fields;
    this.apiEndpoint = _.get(data, 'apiEndpoint');
    this.queryParams = _.get(data, 'queryParams', {});
    this.title = _.get(data, 'title');
    this.titleService.setTitle(this.title);
    this.loadAll();
  }

  save(): void {
    this.httpClient
      .post(this.apiEndpoint, jsyaml.dump(this.model), {
        params: createRequestOption(this.queryParams),
        observe: 'response',
        responseType: 'text'
      })
      .subscribe(() => this.loadAll());
  }
}
