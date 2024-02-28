import { Component, Input } from '@angular/core';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-detail',
  template: `
    <ul class="list-unstyled">
      <li *ngFor="let c of columns" class="d-flex">
        <h6 class="w-50">
          <strong [jhiTranslate]="ns + '.label.' + c" [ngbTooltip]="ns + '.help.' + c | translate">{{ c }}</strong>
        </h6>
        <div class="w-50">{{ _.get(entity, c) }}</div>
      </li>
    </ul>
  `,
  styles: []
})
export class DetailWidgetComponent {
  _ = _;

  @Input() entity: any;

  @Input() columns: string[] = [];

  @Input() ns = 'entity';

  constructor() {}
}
