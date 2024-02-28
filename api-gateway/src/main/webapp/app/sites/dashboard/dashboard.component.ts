import { Component, OnInit } from '@angular/core';
import { EntityService } from 'app/common/model/entity.service';
import { filter, map, tap } from 'rxjs/operators';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  isReady = false;
  sites: any[] = [];
  constructor(private entityService: EntityService) {}

  ngOnInit(): void {
    this.entityService
      .query({ size: 1000, type: 'SITE' }, 'api/nodes')
      .pipe(
        filter(res => res.ok),
        map(res => res.body || []),
        tap(() => (this.isReady = true))
      )
      .subscribe(res => (this.sites = _.chunk(res, 6)));
  }

  trackId(index: number, item: any): string {
    return item.id;
  }
  getLogoSrc(fileId: string): string {
    return `api/public/static/${fileId}`;
  }
}
