import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  heroEnvelope, heroSquares2x2, heroTruck,
  heroLockClosed, heroCurrencyDollar,heroMap,
  heroEye, heroArrowRightOnRectangle,
  heroEyeSlash, heroPower,
  heroArrowRight, heroUser, heroPhone, heroHome, heroArrowUpTray,
  heroClock, heroInformationCircle, heroChevronDown, heroChevronUp,
  heroArrowsUpDown, heroHashtag
} from '@ng-icons/heroicons/outline';
import { heroBoltSolid, heroBoltSlashSolid } from '@ng-icons/heroicons/solid';
import { ionLocationOutline, ionCarOutline } from '@ng-icons/ionicons';
import { routes } from './app.routes';
import { provideIcons } from '@ng-icons/core';
import { authInterceptor } from './security/auth.interceptor';
import { matAssistantPhotoOutline, matAccountCircleOutline } from '@ng-icons/material-icons/outline'
import { bootstrapClock, bootstrap123, bootstrapType } from '@ng-icons/bootstrap-icons'
import { mynaBaby } from '@ng-icons/mynaui/outline'
import { phosphorDog } from '@ng-icons/phosphor-icons/regular'
import { boxCar } from '@ng-icons/boxicons/regular'
import { hugeCar03 } from '@ng-icons/huge-icons'
import { featherFile } from '@ng-icons/feather-icons'

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])), // Use imported interceptor
    provideIcons({
      heroEnvelope, heroBoltSlashSolid,
      heroLockClosed, heroBoltSolid,
      heroEye, heroPower, heroArrowRightOnRectangle,
      heroEyeSlash, heroCurrencyDollar, heroMap,
      heroArrowRight, heroSquares2x2, heroTruck,
      heroUser, heroArrowUpTray, heroHome, heroPhone,
      heroClock, heroInformationCircle, heroChevronDown,
      heroChevronUp, heroArrowsUpDown, ionLocationOutline,
      matAssistantPhotoOutline, bootstrapClock, heroHashtag,
      mynaBaby, phosphorDog, bootstrap123, ionCarOutline,
      boxCar, bootstrapType, hugeCar03, matAccountCircleOutline,
      featherFile
    })
  ]
};
