import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
// + title
import { createRequestOption } from 'app/shared/util/request-util';
// + search
import * as _ from 'lodash';

// @link https://swimlane.gitbook.io/ngx-charts/examples/line-area-charts/stacked-area-chart
@Component({
  selector: 'jhi-charts',
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.scss']
})
export class ChartsComponent implements OnInit {
  _ = _;
  multi: any[] = [];
  // [  {
  //     'name': 'Germany',
  //     'series': [
  //       { 'name': '1990','value': 62000000 },
  //       { 'name': '2010','value': 73000000 },
  //       { 'name': '2011','value': 89400000 }
  //     ]
  //   }
  // ];
  view: any[] = [700, 300];

  // options
  legend = true;
  legendPosition = 'right';
  showLabels = true;
  animations = true;
  xAxis = true;
  yAxis = true;
  showYAxisLabel = true;
  showXAxisLabel = true;
  // xAxisLabel = 'Year';
  // yAxisLabel = 'Population';
  timeline = true;

  colorScheme = {
    domain: ['#5AA454', '#E44D25', '#CFC0BB', '#7aa3e5', '#a8385d', '#aae3f5']
  };
  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.getChartData();
  }

  onSelect(event: any): void {
    // eslint-disable-next-line no-console
    console.log('selected', event);
  }
  getChartData(): void {
    const query = `increase(incoming_call_total_seconds_count[1d])`;
    const start = moment(moment().subtract(7, 'days')).format('X');
    const end = moment(moment().format('YYYY-MM-DDTHH:00')).format('X');
    const step = '1d';
    this.httpClient
      .get('/prometheus/api/v1/query_range', { params: createRequestOption({ query, start, end, step }), observe: 'response' })
      .pipe(
        filter(res => res.ok),
        map(res => res.body || []),
        map(res =>
          _.get(res, 'data.result', []).map((i: any) => ({
            name: i.metric.exception,
            series: _.map(i.values, v => ({ name: moment(new Date(v[0] * 1000)).format('YYYY-MM-DD'), value: parseInt(v[1], 10) }))
          }))
        )
      )
      .subscribe(stat => (this.multi = stat));
  }
}
