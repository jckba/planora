import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.services';

export const onboardingGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasOnboardingToken()) {
    return true;
  }

  if (authService.hasAccessToken()) {
    //TODO redirigir a ruta principal cuando exista
    return router.createUrlTree(['/login']);
  }

  return router.createUrlTree(['/login']);
};
