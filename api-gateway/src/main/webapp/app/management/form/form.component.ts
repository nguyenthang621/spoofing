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

@Component({
  selector: 'jhi-form',
  templateUrl: './form.component.html',
  styleUrls: ['./form.component.scss']
})
export class FormComponent implements OnInit {
  _ = _;
  title = '';
  // + config
  fields: any[] = [];
  settings: any = {};
  hideBtn = false;
  model: any = {};
  apiEndpoint = '';
  queryParams: any = {};
  form = new FormGroup({});
  items: any[] = [];

  errorMsg = '';
  successMsg = '';

  constructor(
    private entityService: EntityService,
    private titleService: Title,
    private httpClient: HttpClient,
    private alertService: JhiAlertService,
    private activatedRoute: ActivatedRoute
  ) {
    this.settings = {};
  }

  loadAll(): void {
    if (this.apiEndpoint) {
      this.httpClient
        .get(SERVER_API_URL + this.apiEndpoint, { params: createRequestOption(this.queryParams), observe: 'response' })
        .pipe(
          filter(res => res.ok),
          map(res => res.body || {})
        )
        .subscribe(
          res => (this.model = res),
          (err: HttpErrorResponse) => this.onError(err)
        );
    }
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ settings }) => this.initialize(settings));
  }
  previousState(): void {
    window.history.back();
  }

  protected onError(err: HttpErrorResponse): void {
    // eslint-disable-next-line no-console
    console.error('HttpErrorResponse', err);
    this.alertService.error(this.errorMsg ? _.template(this.errorMsg)(err) : err.error.detail || err.statusText || err.message);
  }

  // Populate data from settings
  initialize(data: any): void {
    this.fields = data.fields;
    this.apiEndpoint = _.get(data, 'apiEndpoint');
    this.queryParams = _.get(data, 'queryParams', {});
    this.errorMsg = data.errorMsg;
    this.successMsg = data.successMsg;
    this.title = _.get(data, 'title');
    this.titleService.setTitle(this.title);
    this.hideBtn = data.hideBtn;
    this.loadAll();
  }

  save(): void {
    if (this.apiEndpoint) {
      this.httpClient
        .post(this.apiEndpoint, this.model, {
          params: createRequestOption(this.queryParams),
          observe: 'response'
        })
        .subscribe(() => this.loadAll());
    }
  }
}
