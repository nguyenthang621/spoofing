import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from 'app/shared/shared.module';
import { ConsulHealthComponent } from './consul-health.component';

import { consulHealthRoutes } from './consul-health.route';
import { HealthDetailsComponent } from './health-details/health-details.component';

@NgModule({
  imports: [SharedModule, RouterModule.forChild(consulHealthRoutes)],
  declarations: [ConsulHealthComponent, HealthDetailsComponent]
})
export class ConsulHealthModule {}
