import { Routes } from '@angular/router';
import { Login } from './pages/auth/login/login';
import { Register } from './pages/auth/register/register';
import { UnregisteredHome } from './pages/home/unregistered/unregistered-home';
import { authGuard } from './guards/auth.guard';
import { RideBooking } from './pages/home/passenger/components/ride-booking/ride-booking';
import { HomePassenger } from './pages/home/passenger/passenger-home';
import {
  RideEstimationForm
} from './pages/home/unregistered/components/ride-estimation/ride-estimation-form/ride-estimation-form';
import {
  RideEstimationResult
} from './pages/home/unregistered/components/ride-estimation/ride-estimation-result/ride-estimation-result';

export const routes: Routes = [
  // Public routes
  {
    path: 'unregistered',
    component: UnregisteredHome,
    children: [
      { path: '', redirectTo: 'form', pathMatch: 'full' },
      { path: 'form', component: RideEstimationForm },
      { path: 'result', component: RideEstimationResult }
    ]},
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', loadComponent: () => import('./pages/auth/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./pages/auth/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: 'password-change', loadComponent: () => import('./pages/password-change/password-change').then(m => m.PasswordChange) },
  { path: 'activate', loadComponent: () => import('./pages/auth/activate-account/activate-account').then(m => m.ActivateAccount) },
  { path: 'password-setup', loadComponent: () => import('./pages/auth/password-setup/password-setup').then(m => m.PasswordSetup) },

  // Driver routes
  {
  path: 'driver',
  canActivate: [authGuard],
  children: [
    {
      path: 'home',
      loadComponent: () =>
        import('./pages/home/driver/driver-home').then(m => m.HomeDriver),
      children: [
        {
          path: '',
          loadComponent: () =>
            import('./pages/home/driver/components/driver-waiting/driver-waiting')
              .then(m => m.DriverWaiting)
        },
        {
          path: 'ride-assign/:id',
          loadComponent: () =>
            import('./pages/home/driver/components/driver-ride-assignment/driver-ride-assignment')
              .then(m => m.DriverRideAssignment)
        }
      ]
    },
      {
        path: 'ride-tracking/:id',
        loadComponent: () => import('./pages/ride-tracking/driver/driver-ride-tracking').then(m => m.DriverRideTrackingComponent)
      },
      {
        path: 'history',
        loadComponent: () => import('./pages/ride-history/driver/driver-history').then(m => m.DriverHistory),
        canActivate: [authGuard]
      },
      {
        path: 'support',
        loadComponent: () => import('./pages/support-chat/driver/driver-support').then(m => m.DriverSupport)
      },
      {
        path: 'profile',
        loadComponent: () => import('./pages/profile/driver-profile/driver-profile').then(m => m.DriverProfile)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Passenger routes
  {
    path: 'passenger',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        component: HomePassenger,
        children: [
          {
            path: 'ride-wait/:id',
            loadComponent: () => import('./pages/home/passenger/components/ride-wait/ride-wait').then(m => m.RideWait)
          },
          {
            path: 'ride-booking', component: RideBooking
          },
          {
            path: '',
            redirectTo: 'ride-booking',
            pathMatch: 'full'
          }
        ]
      },
      {
        path: 'support',
        loadComponent: () => import('./pages/support-chat/passenger/passenger-support').then(m => m.PassengerSupport)
      },
      {
        path: 'history',
        loadComponent: () => import('./pages/ride-history/passenger/passenger-history').then(m => m.PassengerHistory)
      },
      {
        path: 'ride-rating/:id',
        loadComponent: () => import('./pages/ride-rating/ride-rating').then(m => m.RideRatingComponent)
      },
      {
        path: 'ride-tracking/:id',
        loadComponent: () => import('./pages/ride-tracking/passenger/passenger-ride-tracking').then(m => m.PassengerRideTrackingComponent)
      },
      {
      path: 'notifications',
      loadComponent: () => import('./pages/notifications/passenger/passenger-notifications').then(m => m.PassengerNotificationsComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./pages/profile/base-profile/base-profile').then(m => m.BaseProfile)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Admin routes
  {
    path: 'admin',
    canActivate: [authGuard],
    children: [
      {
        path: 'home',
        loadComponent: () => import('./pages/home/admin/admin-home').then(m => m.AdminHome)
      },
      {
        path: 'support',
        loadComponent: () => import('./pages/support-chat/admin/admin-support').then(m => m.AdminSupport)
      },
      {
        path: 'ride-supervision',
        loadComponent: () => import('./pages/ride-supervision/admin-ride-supervision').then(m => m.AdminRideSupervisionComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./pages/profile/base-profile/base-profile').then(m => m.BaseProfile)
      },
      {
        path: 'history',
        loadComponent: () => import('./pages/ride-history/admin/admin-history').then(m => m.AdminRideHistoryComponent)
      },
      {
        path: 'notifications',
        loadComponent: () => import('./pages/notifications/admin/admin-notifications').then(m => m.AdminNotificationsComponent)
      },
      {
        path: 'driver-register',
        loadComponent: () => import('./pages/auth/driver-register/driver-register').then(m => m.DriverRegister)
      },
      {
        path: 'change-requests',
        loadComponent: () => import('./pages/profile/change-requests/change-requests').then(m => m.ChangeRequests)
      },
      {
        path: 'pricelist',
        loadComponent: () => import('./pages/pricelist/pricelist-admin').then(m => m.PricelistAdmin)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: 'unregistered' }
];
