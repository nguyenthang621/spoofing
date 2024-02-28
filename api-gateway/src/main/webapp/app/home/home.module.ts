import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedCommonModule } from 'app/common/common.module';
import { HOME_ROUTES } from './home.route';
import { HomeComponent } from './home.component';
// + rendering markdown pages
import { MarkdownModule } from 'ngx-markdown';
import { DocsComponent } from './docs/docs.component';
// + chart
import { NgxChartsModule } from '@swimlane/ngx-charts';
import { ChartsComponent } from './charts/charts.component';
// + others
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SummaryComponent } from './summary/summary.component';
import { TracingComponent } from './tracing/tracing.component';

@NgModule({
  imports: [SharedCommonModule, MarkdownModule.forRoot(), NgxChartsModule, RouterModule.forChild(HOME_ROUTES)],
  declarations: [HomeComponent, DocsComponent, LoginComponent, DashboardComponent, ChartsComponent, SummaryComponent, TracingComponent]
})
export class HomeModule {}
