import { Component, OnInit } from '@angular/core';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { HttpResponse } from '@angular/common/http';

import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, combineLatest } from 'rxjs';
import { tap } from 'rxjs/operators';
// + Form Builder
import * as _ from 'lodash';
import * as moment from 'moment';
import { AccountService } from 'app/core/auth/account.service';
import { EntityService } from 'app/common/model/entity.service';
import { Title } from '@angular/platform-browser';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';

@Component({
  selector: 'jhi-data-update',
  templateUrl: './data-update.component.html'
})
export class DataUpdateComponent implements OnInit {
  _ = _;
  title = '';
  isReady = false;
  isSaving = false;
  model: any = {};
  fields: FormlyFieldConfig[] = [];
  prop = '';
  svc = '';
  debug = DEBUG_INFO_ENABLED;
  apiEndpoint = '';
  editForm = new FormGroup({});
  options: any = {
    formState: {
      moment
    }
  };

  constructor(
    private accountService: AccountService,
    private dataService: EntityService,
    private titleService: Title,
    private activatedRoute: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.isSaving = false;
    combineLatest(
      this.accountService.identity().pipe(tap(account => (this.options.formState.account = account))),
      this.activatedRoute.data.pipe(
        tap(({ templateFile, model, copy }) => {
          // data model
          this.model = copy ? _.omit(model, 'id') : model;
          this.options.formState.mainModel = this.model;
          // namespace
          this.svc = templateFile.svc;
          this.prop = templateFile.prop;
          // + allow use template placeholders
          this.title = _.template(_.get(templateFile, this.model.id ? 'config.title.update' : 'config.title.create', 'createOrEditData'))(
            this.model
          );
          this.titleService.setTitle(this.title);
          // + apiEndpoint and params
          this.apiEndpoint = _.get(templateFile, 'config.apiEndpoint', templateFile.apiEndpoint);
          // + form rendering
          this.fields = _.get(templateFile, 'config.fields', []);
        })
      )
    ).subscribe(() => (this.isReady = true));
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    if (_.get(this.model, 'id') !== undefined) {
      this.subscribeToSaveResponse(this.dataService.update(this.model, this.apiEndpoint));
    } else {
      this.subscribeToSaveResponse(this.dataService.create(this.model, this.apiEndpoint));
    }
  }

  private subscribeToSaveResponse(result: Observable<HttpResponse<any>>): void {
    result.subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  private onSaveSuccess(): void {
    this.isSaving = false;
    this.previousState();
  }

  private onSaveError(): void {
    this.isSaving = false;
  }
}
