import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedCommonModule } from 'app/common/common.module';
import { MANAGEMENT_ROUTES } from './management.route';
// + component
import { FormComponent } from './form/form.component';
import { SettingsComponent } from './settings/settings.component';
import { ConfigurationComponent } from './configuration/configuration.component';

@NgModule({
  imports: [SharedCommonModule, RouterModule.forChild(MANAGEMENT_ROUTES)],
  declarations: [FormComponent, SettingsComponent, ConfigurationComponent]
})
export class ManagementModule {}
