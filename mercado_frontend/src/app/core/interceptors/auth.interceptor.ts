import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const authorization = authService.getAuthorizationHeader();
  const isApiRequest = request.url.startsWith(environment.apiUrl);
  const isAuthRequest = request.url.includes('/auth/');

  if (!authorization || !isApiRequest || isAuthRequest) {
    return next(request);
  }

  return next(request.clone({
    setHeaders: {
      Authorization: authorization
    }
  }));
};
