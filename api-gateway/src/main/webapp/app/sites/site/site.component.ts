import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EntityService } from 'app/common/model/entity.service';
import { filter, map, tap } from 'rxjs/operators';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-site',
  templateUrl: './site.component.html',
  styleUrls: ['./site.component.scss']
})
export class SiteComponent implements OnInit {
  isReady = false;
  site: any;
  types: any[] = [];
  constructor(private activatedRoute: ActivatedRoute, private entityService: EntityService) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ site }) => this.loadSiteContentTypes(site));
  }
  loadSiteContentTypes(site: any): void {
    this.site = site;
    this.entityService
      .query({ tags: site.id, type: 'CONTENT_TYPE', state: 200 }, 'api/nodes')
      .pipe(
        filter(res => res.ok),
        map(res => res.body || []),
        tap(() => (this.isReady = true))
      )
      .subscribe(res => (this.types = _.chunk(res, 6)));
  }

  trackId(index: number, item: any): string {
    return item.id;
  }
}
