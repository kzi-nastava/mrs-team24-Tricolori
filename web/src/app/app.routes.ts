import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { UnregisteredHome } from './features/home/unregistered/unregistered-home';

export const routes: Routes = [
  // Public routes
  { path: '', component: UnregisteredHome },
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  { path: 'forgot-password', loadComponent: () => import('./features/auth/forgot-password/forgot-password').then(m => m.ForgotPassword) },
  { path: 'reset-password', loadComponent: () => import('./features/auth/reset-password/reset-password').then(m => m.ResetPassword) },
  { path: 'password-change', loadComponent: () => import('./features/password-change/password-change').then(m => m.PasswordChange) },

  // Driver routes
  {
    path: 'driver',
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/home/driver/driver-home').then(m => m.HomeDriver)
      },
      {
        path: 'history',
        loadComponent: () => import('./features/driver-history/driver-history').then(m => m.DriverHistory)
      },
      {
        path: 'support',
        loadComponent: () => import('./features/support-chat/driver/driver-support').then(m => m.DriverSupport)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then(m => m.Profile)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Passenger routes
  {
    path: 'passenger',
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/home/passenger/passenger-home').then(m => m.HomePassenger)
      },
      {
        path: 'support',
        loadComponent: () => import('./features/support-chat/passenger/passenger-support').then(m => m.PassengerSupport)
      },
      {
        path: 'history',
        loadComponent: () => import('./features/ride-history/passenger/passenger-history').then(m => m.PassengerHistory)
      },
      {
        path: 'ride-rating/:id',
        loadComponent: () => import('./features/ride-rating/ride-rating').then(m => m.RideRatingComponent)
      },
      {
        path: 'ride-tracking/:id',
        loadComponent: () => import('./features/ride-tracking/ride-tracking').then(m => m.RideTrackingComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then(m => m.Profile)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  // Admin routes
  {
    path: 'admin',
    children: [
      {
        path: 'home',
        loadComponent: () => import('./features/home/admin/admin-home').then(m => m.AdminHome)
      },
      {
        path: 'support',
        loadComponent: () => import('./features/support-chat/admin/admin-support').then(m => m.AdminSupport)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/profile/profile').then(m => m.Profile)
      },
      {
        path: 'driver-register',
        loadComponent: () => import('./features/auth/driver-register/driver-register').then(m => m.DriverRegister)
      },
      { path: '', redirectTo: 'home', pathMatch: 'full' }
    ]
  },

  { path: '**', redirectTo: '' }
];