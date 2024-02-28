import { Route } from '@angular/router';

import { SettingsComponent } from './settings.component';

export const SettingsRoute: Route = {
  path: '',
  component: SettingsComponent,
  data: {
    pageTitle: 'global.menu.admin.settings'
  }
};
