import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedCommonModule } from 'app/common/common.module';
import { DataComponent } from './data.component';
import { DataDetailComponent } from './data-detail.component';
import { DataUpdateComponent } from './data-update.component';
import { DataImportComponent } from './data-import.component';
import { dataRoute } from './data.route';

const ENTITY_STATES = [...dataRoute];

@NgModule({
  imports: [SharedCommonModule, RouterModule.forChild(ENTITY_STATES)],
  declarations: [DataComponent, DataDetailComponent, DataUpdateComponent, DataImportComponent],
  entryComponents: [DataComponent, DataUpdateComponent]
})
export class GatewayDataModule {}
