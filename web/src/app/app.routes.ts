import { Routes } from '@angular/router';
import { Login } from './features/auth/login/login';
import { Register } from './features/auth/register/register';
import { ResetPassword } from './features/auth/reset-password/reset-password';
import { Profile } from './features/profile/profile';
import { PasswordChange } from './features/password-change/password-change';
import { UnregisteredHome } from './features/home/unregistered/unregistered-home';
import { DriverHistory } from './features/ride-history/driver-history';

export const routes: Routes = [
  {path: '',component: UnregisteredHome},
  {path: "login", component: Login},
  {path: "register", component: Register},
  {path: "reset-password", component: ResetPassword},
  {path: "profile", component: Profile},
  {path: "password-change", component: PasswordChange},
  {path: "driver-history", component: DriverHistory},
  {path: "unregistered-home", component: UnregisteredHome}
];