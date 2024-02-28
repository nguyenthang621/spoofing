import { Component, OnInit } from '@angular/core';
import { FieldType } from '@ngx-formly/core';
import { FormControl } from '@angular/forms';
import { DATE_FORMAT } from 'app/shared/constants/input.constants';
import * as moment from 'moment';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-formly-field-datepicker',
  template: `
    <div class="input-group">
      <div class="input-group-prepend" *ngIf="!to.required">
        <button class="btn btn-sm btn-outline-secondary" (click)="clear()" type="button" [disabled]="!date.value">
          <fa-icon icon="times"></fa-icon>
        </button>
      </div>
      <input
        class="form-control"
        [formControl]="date"
        [formlyAttributes]="field"
        ngbDatepicker
        #d="ngbDatepicker"
        (dateSelect)="onDateSelect()"
      />
      <div class="input-group-append">
        <button class="btn btn-outline-secondary calendar" (click)="d.toggle()" type="button">
          <fa-icon icon="calendar-alt"></fa-icon>
        </button>
      </div>
    </div>
  `
})
export class DateTypeComponent extends FieldType implements OnInit {
  defaultOptions = {
    templateOptions: {
      wrappers: ['form-field']
    }
  };
  date = new FormControl();
  // Store the value in string when the form update
  ngOnInit(): void {
    this.formControl.valueChanges.subscribe(() => this.onFormValueChange());
    this.onFormValueChange();
  }
  onDateSelect(): void {
    this.formControl.setValue(moment(this.date.value).format(DATE_FORMAT));
  }
  onFormValueChange(): void {
    if (this.formControl.value && _.isString(this.formControl.value)) {
      this.date.setValue(moment(this.formControl.value));
    }
  }
  clear(): void {
    this.formControl.setValue(null);
    this.date.setValue(null);
  }
}
