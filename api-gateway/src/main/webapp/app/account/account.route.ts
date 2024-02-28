import { Routes } from '@angular/router';

import { passwordRoute } from './password/password.route';
import { passwordResetFinishRoute } from './password-reset/finish/password-reset-finish.route';
import { passwordResetInitRoute } from './password-reset/init/password-reset-init.route';
import { settingsRoute } from './settings/settings.route';

const ACCOUNT_ROUTES = [passwordRoute, passwordResetFinishRoute, passwordResetInitRoute, settingsRoute];

export const accountState: Routes = [
  {
    path: '',
    children: ACCOUNT_ROUTES
  }
];
