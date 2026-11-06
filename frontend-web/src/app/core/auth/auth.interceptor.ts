import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../services/auth.services';
import {API_CONFIG} from '../config/api.config';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  if (!req.url.startsWith(API_CONFIG.baseUrl)) {
    return next(req);
  }

  let request = req.clone({
    withCredentials: true
  })

  const isPublicEndpoint =
    /\/auth\/(login|register|email-verification\/confirm|email-verification\/resend|google\/session)(?:\?|$)/.test(req.url) ||
    /\/currencies(?:\?|$)/.test(req.url);

  if (isPublicEndpoint) {
    return next(request);
  }

  const token = authService.getTokenForRequest(req.url);

  if (token) {
    request = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(request);
};

