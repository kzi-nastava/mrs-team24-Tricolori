import { Routes } from '@angular/router';
import {Login} from './features/auth/login/login';
import {Register} from './features/register/register';

export const routes: Routes = [
  {path: "login", component: Login},
  {path: "register", component: Register}
];
