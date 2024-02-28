import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/core';
import { Router } from '@angular/router';
import { HttpClient, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { JhiAlertService } from 'ng-jhipster';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { plainToFlattenObject } from 'app/common/util/request-util';
import * as _ from 'lodash';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';

@Component({
  selector: 'jhi-formly-button',
  template: `
    <div>
      <button [type]="to.type || 'button'" [ngClass]="'btn btn-' + (to.btnType ? to.btnType : 'outline-primary')" (click)="onClick($event)">
        <fa-icon *ngIf="to.icon" [icon]="to.icon"></fa-icon>
        <span [innerHtml]="to.label"></span>
      </button>
    </div>
  `
})
export class ButtonTypeComponent extends FieldType {
  defaultOptions = {
    templateOptions: {
      wrappers: ['form-field']
    }
  };
  constructor(private router: Router, protected httpClient: HttpClient, private alertService: JhiAlertService) {
    super();
  }
  onClick($event: any): void {
    if (this.to.onClick) {
      this.to.onClick($event);
    } else if (this.to.apiEndpoint) {
      this.createRequest()
        .pipe(
          filter(res => res.ok),
          map(res => res.body)
        )
        .subscribe(
          res => this.processResponse(res),
          err => this.onError(err)
        );
    }
  }

  createRequest(): Observable<HttpResponse<any>> {
    const params = createRequestOption(_.omitBy(plainToFlattenObject(this.to.params), _.isNull));
    const body = _.omitBy(this.to.body, _.isNull);
    if (this.to.method) {
      return this.httpClient.request<HttpResponse<any>>(this.to.method, SERVER_API_URL + this.to.apiEndpoint, {
        params,
        body,
        observe: 'response'
      });
    } else if (_.isEmpty(body)) {
      return this.httpClient.get<HttpResponse<any>>(SERVER_API_URL + this.to.apiEndpoint, {
        params,
        observe: 'response'
      });
    } else {
      return this.httpClient.post<HttpResponse<any>>(SERVER_API_URL + this.to.apiEndpoint, body, {
        params,
        observe: 'response'
      });
    }
  }

  processResponse(res: any): void {
    this.formControl.setValue(res);
    this.alertService.success(this.to.okMsg || 'Data updated');
    if (this.to.returnUrl) {
      this.router.navigateByUrl(this.to.returnUrl);
    }
  }

  onError(err: HttpErrorResponse): void {
    // eslint-disable-next-line no-console
    console.error('HttpErrorResponse', err);
    this.alertService.error(this.to.errorMsg ? _.template(this.to.errorMsg)(err) : err.error.detail || err.statusText || err.message);
  }
}
