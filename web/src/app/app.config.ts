import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import {
  heroEnvelope, heroSquares2x2, heroTruck,
  heroLockClosed, heroCurrencyDollar,
  heroEye, heroArrowRightOnRectangle,
  heroEyeSlash, heroPower,
  heroArrowRight, heroUser, heroPhone, heroHome, heroArrowUpTray,
  heroClock, heroInformationCircle, heroChevronDown, heroChevronUp,
  heroArrowsUpDown
} from '@ng-icons/heroicons/outline';
import {heroBoltSolid, heroBoltSlashSolid} from '@ng-icons/heroicons/solid'
import {ionLocationOutline} from '@ng-icons/ionicons'

import { routes } from './app.routes';
import { provideIcons } from '@ng-icons/core';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {authInterceptor} from './shared/model/auth.interceptor';

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
      heroUser, heroArrowUpTray, heroHome, heroPhone,
      heroClock, heroInformationCircle, heroChevronDown,
      heroChevronUp, heroArrowsUpDown, ionLocationOutline
    }),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
