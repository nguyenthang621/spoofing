import { Component } from '@angular/core';
import { FieldArrayType } from '@ngx-formly/core';

@Component({
  selector: 'jhi-formly-card',
  template: `
    <div class="card">
      <p class="card-header">{{ to.label }}</p>
      <div class="card-body" [ngClass]="to.bodyClassName">
        <formly-field [field]="f" *ngFor="let f of field.fieldGroup"></formly-field>
      </div>
    </div>
  `
})
export class FormlyCardTypeComponent extends FieldArrayType {}
