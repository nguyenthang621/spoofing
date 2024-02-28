import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedCommonModule } from 'app/common/common.module';
import { SITE_ROUTES } from './sites.route';
import { DashboardComponent } from './dashboard/dashboard.component';
import { DataComponent } from './data/data.component';
import { SiteComponent } from './site/site.component';
import { DataDetailComponent } from './data-detail/data-detail.component';
import { DataUpdateComponent } from './data-update/data-update.component';
import { DataImportComponent } from './data-import/data-import.component';

@NgModule({
  imports: [SharedCommonModule, RouterModule.forChild(SITE_ROUTES)],
  declarations: [DashboardComponent, DataComponent, SiteComponent, DataDetailComponent, DataUpdateComponent, DataImportComponent]
})
export class SitesModule {}
