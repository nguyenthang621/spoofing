import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { filter, map } from 'rxjs/operators';
import { SERVER_API_URL } from 'app/app.constants';

@Component({
  selector: 'jhi-consul-health',
  templateUrl: './consul-health.component.html',
  styleUrls: ['./consul-health.component.scss']
})
export class ConsulHealthComponent implements OnInit {
  healthIndicators: any[] = [];

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.refresh();
  }
  refresh(): void {
    this.httpClient
      .get<any[]>(SERVER_API_URL + 'api/consul/health', { observe: 'response' })
      .pipe(
        filter(res => res.ok),
        map(res => res.body || [])
      )
      .subscribe(res => (this.healthIndicators = res));
  }
}
