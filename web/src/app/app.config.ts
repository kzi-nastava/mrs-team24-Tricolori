import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  heroEnvelope,
  heroLockClosed,
  heroEye,
  heroEyeSlash,
  heroArrowRight, heroUser, heroPhone, heroHome, heroArrowUpTray
} from '@ng-icons/heroicons/outline';

import { routes } from './app.routes';
import { provideIcons } from '@ng-icons/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideIcons({
      heroEnvelope,
      heroLockClosed,
      heroEye,
      heroEyeSlash,
      heroArrowRight,
      heroUser, heroArrowUpTray, heroHome, heroPhone
    })
  ]
};
