import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../../features/auth/auth.services';
import {API_CONFIG} from '../config/api.config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  const token = authService.getAccessToken();
  const isApiRequest = req.url.startsWith(API_CONFIG.baseUrl);

  if (!isApiRequest) {
    return next(req);
  }

  const isLocalLogin = req.url.endsWith('/auth/login');
  const isGoogleSession = req.url.endsWith('/auth/google/session');
  const isGoogleComplete = req.url.endsWith('/auth/google/complete');
  const isGoogleEndpoint = req.url.includes('/auth/google');

  let request = req;

  // Peticiones de google envian la cooke de sesion oauth
  if (!isGoogleEndpoint) {
    request = request.clone({
      withCredentials: true,
    })
  }

  const requiresBearerToken =
    !isLocalLogin &&
    !isGoogleSession &&
    !isGoogleComplete;

  if (token && requiresBearerToken) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      }
    });
  }

  return next(request);
}
