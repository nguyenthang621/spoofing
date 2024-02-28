import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormGroup } from '@angular/forms';
import { filter, map, tap } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';
import * as moment from 'moment';
// + title
import { createRequestOption } from 'app/shared/util/request-util';
// + search
import * as _ from 'lodash';
import { JhiAlertService } from 'ng-jhipster';

@Component({
  selector: 'jhi-summary',
  templateUrl: './summary.component.html'
})
export class SummaryComponent implements OnInit, OnDestroy {
  _ = _;
  cnt = 0;
  rows: any[] = [];
  apiEndpoint = '';
  // Page reload
  searchModel: any;
  searchForm = new FormGroup({});
  searchFields = [
    {
      fieldGroupClassName: 'row',
      fieldGroup: [
        {
          key: 'from',
          type: 'date',
          className: 'col-md-4',
          templateOptions: {
            addonLeft: {
              text: 'From Date'
            }
          }
        },
        {
          key: 'to',
          type: 'date',
          className: 'col-md-4',
          templateOptions: {
            addonLeft: {
              text: 'To Date'
            }
          }
        },
        //
        {
          key: 'search',
          type: 'button',
          className: 'col-md-2 col-6',
          templateOptions: {
            icon: 'search',
            type: 'submit',
            btnType: 'primary btn-block',
            label: 'Search'
          }
        },
        {
          key: 'reset',
          type: 'button',
          className: 'col-md-2 col-6',
          templateOptions: {
            icon: 'ban',
            btnType: 'secondary btn-block',
            label: 'Reset',
            onClick: () => this.resetData()
          }
        }
      ]
    }
  ];

  constructor(private httpClient: HttpClient, private alertService: JhiAlertService, private activatedRoute: ActivatedRoute) {}

  resetData(): void {
    this.searchModel = {
      from: moment().format('YYYY-MM-01'),
      to: moment().format('YYYY-MM-DD')
    };
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ apiEndpoint }) => {
      this.apiEndpoint = apiEndpoint;
      this.resetData();
      this.searchData();
    });
  }

  ngOnDestroy(): void {}
  // Get Today Statistics
  // + total call: increase(asterisk_calls_sum[24h])
  // + uptime asterisk: asterisk_core_uptime_seconds
  // + bypass: incoming_bypass_total
  // + reject: incoming_reject_total
  searchData(): void {
    this.httpClient
      .get<any>(this.apiEndpoint, { params: createRequestOption(this.searchModel), observe: 'response' })
      .pipe(
        filter(res => res.ok),
        map(res => res.body || {}),
        tap(res => (this.cnt = res.cnt || 0))
      )
      .subscribe(
        data => (this.rows = data),
        err => this.alertService.error(err.error?.detail || err.message)
      );
  }
}
