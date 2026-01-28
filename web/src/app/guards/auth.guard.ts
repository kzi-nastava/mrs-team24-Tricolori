import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  } else {
    // If user is not logged in, we send him to login page
    // BUT WE SAVE RETURN URL so we can auto-return him where he wanted 
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }
};