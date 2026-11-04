import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.services';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasAccessToken()) {
    return true;
  }

  if (authService.hasOnboardingToken()) {
    return router.createUrlTree(['/complete-registration']);
  }

  return router.createUrlTree(['/login']);
};
