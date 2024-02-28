import { Component, OnInit, Input } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { filter, map } from 'rxjs/operators';
import { SERVER_API_URL } from 'app/app.constants';
import { plainToFlattenObject } from 'app/common/util/request-util';
import * as _ from 'lodash';
@Component({
  selector: 'jhi-health-details',
  templateUrl: './health-details.component.html',
  styleUrls: ['./health-details.component.scss']
})
export class HealthDetailsComponent implements OnInit {
  @Input() healthIndicator: any;
  meta: any;
  properties: any;
  output = '';
  metadata = '';

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.parseOutput(this.healthIndicator.output, this.healthIndicator.serviceName);
  }

  // consul status: UNKNOWN, 	PASSING, 	WARNING
  getBadgeClass(statusState: string): string {
    if (statusState === 'CRITICAL') {
      return 'badge-danger';
    } else if (statusState === 'PASSING') {
      return 'badge-success';
    } else if (statusState === 'WARNING') {
      return 'badge-warning';
    } else {
      return 'badge-dark';
    }
  }

  parseOutput(output: string, service: string): void {
    try {
      this.meta = JSON.parse(output.substr(output.indexOf('Output: ') + 'Output: '.length));
      this.httpClient
        .get(SERVER_API_URL + `assets/admin/consul-health/${service}.html.tpl`, { responseType: 'text', observe: 'response' })
        .pipe(
          filter(res => res.ok),
          map(res => res.body || '')
        )
        .subscribe(
          (res: string) => (this.metadata = _.template(res)(this.meta)),
          () => (this.properties = plainToFlattenObject(this.meta))
        );
      this.output = output.substr(0, output.indexOf('Output: '));
    } catch (error) {
      this.meta = undefined;
      this.output = output;
    }
  }
}
