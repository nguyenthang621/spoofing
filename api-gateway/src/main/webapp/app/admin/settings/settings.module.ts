import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedCommonModule } from 'app/common/common.module';

import { SettingsComponent } from './settings.component';

import { SettingsRoute } from './settings.route';

@NgModule({
  imports: [SharedCommonModule, RouterModule.forChild([SettingsRoute])],
  declarations: [SettingsComponent]
})
export class SettingsModule {}
