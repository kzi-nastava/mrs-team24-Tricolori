import { Routes } from '@angular/router';
import {Login} from './features/auth/login/login';
import {Register} from './features/auth/register/register';
import { Profile } from './features/profile/profile';
import { PasswordChange } from './features/password-change/password-change';

export const routes: Routes = [
  {path: "login", component: Login},
  {path: "register", component: Register},
  {path: "profile", component: Profile},
  {path: "password-change", component: PasswordChange},
  {path: "driver-history", loadComponent: () => import('./features/ride-history/driver-history').then(m => m.DriverHistory)},
];
