import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  heroEnvelope, heroSquares2x2, heroTruck,
  heroLockClosed, heroCurrencyDollar,
  heroEye, heroArrowRightOnRectangle,
  heroEyeSlash, heroPower,
  heroArrowRight, heroUser, heroPhone, heroHome, heroArrowUpTray
} from '@ng-icons/heroicons/outline';
import {heroBoltSolid, heroBoltSlashSolid} from '@ng-icons/heroicons/solid'

import { routes } from './app.routes';
import { provideIcons } from '@ng-icons/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideIcons({
      heroEnvelope, heroBoltSlashSolid,
      heroLockClosed, heroBoltSolid,
      heroEye, heroPower, heroArrowRightOnRectangle,
      heroEyeSlash, heroCurrencyDollar,
      heroArrowRight, heroSquares2x2, heroTruck,
      heroUser, heroArrowUpTray, heroHome, heroPhone
    })
  ]
};
