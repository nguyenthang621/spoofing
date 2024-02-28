import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import './vendor';
// import { SharedModule } from 'app/shared/shared.module';
import { SharedCommonModule } from 'app/common/common.module';
import { CoreModule } from 'app/core/core.module';
import { AppRoutingModule } from './app-routing.module';
import { HomeModule } from './home/home.module';
import { EntityModule } from './entities/entity.module';
// jhipster-needle-angular-add-module-import JHipster will add new module here
import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';
import { ErrorComponent } from './layouts/error/error.component';
// + spinner
import { NgxSpinnerModule } from 'ngx-spinner';
// + device detector
import { DeviceDetectorModule } from 'ngx-device-detector';
// + service worker
import { ServiceWorkerModule } from '@angular/service-worker';
import { DEBUG_INFO_ENABLED } from 'app/app.constants';

@NgModule({
  imports: [
    // + angular
    BrowserModule,
    BrowserAnimationsModule,
    // + device-detector
    DeviceDetectorModule.forRoot(),
    // + service-worker
    ServiceWorkerModule.register('/service-worker.js', { enabled: !DEBUG_INFO_ENABLED }),
    // SharedModule,
    NgxSpinnerModule,
    SharedCommonModule,
    // + jhipster
    CoreModule,
    HomeModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    EntityModule,
    AppRoutingModule
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, ActiveMenuDirective, FooterComponent],
  bootstrap: [MainComponent]
})
export class GatewayAppModule {}
