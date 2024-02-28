import { Routes } from '@angular/router';

import { ConsulHealthComponent } from './consul-health.component';

export const consulHealthRoutes: Routes = [
  {
    path: '',
    component: ConsulHealthComponent,
    data: {
      pageTitle: 'health.title'
    }
  },
  {
    path: ':service/view',
    component: ConsulHealthComponent,
    data: {
      pageTitle: 'health.title'
    }
  }
];
