import { Routes } from '@angular/router';
import {Login} from './features/auth/login/login';
import {Register} from './features/auth/register/register';

export const routes: Routes = [
  {path: "login", component: Login},
  {path: "register", component: Register},
  {path: "driver-history", loadComponent: () => import('./features/ride-history/driver-history').then(m => m.DriverHistory)},
];
