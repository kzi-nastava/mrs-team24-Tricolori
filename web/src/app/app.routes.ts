import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { ResetPassword } from './features/auth/reset-password/reset-password';
import { Profile } from './features/profile/profile';
import { PasswordChange } from './features/password-change/password-change';
import { UnregisteredHome } from './features/home/unregistered/unregistered-home';

export const routes: Routes = [
   {
    path: '',
    component: UnregisteredHome},
  {path: "login", component: Login},
  {path: "register", component: Register},
  {path: "reset-password", component: ResetPassword},
  {path: "profile", component: Profile},
  {path: "password-change", component: PasswordChange},
  {path: "driver-history", loadComponent: () => import('./features/ride-history/driver-history').then(m => m.DriverHistory)},
  {path: "unregistered-home", loadComponent: () => import('./features/home/unregistered/unregistered-home').then(m => m.UnregisteredHome)}
];
