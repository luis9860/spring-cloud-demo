import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const restauranteId = sessionStorage.getItem('restauranteId');
  const rol = sessionStorage.getItem('rol');
  const token = sessionStorage.getItem('token');

  let headers = req.headers;
  if (restauranteId) headers = headers.set('X-Restaurante-Id', restauranteId);
  if (rol) headers = headers.set('X-Rol', rol);
  if (token) headers = headers.set('Authorization', `Bearer ${token}`);

  return next(req.clone({ headers })).pipe(
    catchError((err) => {
      if (err.status === 401 && !req.url.includes('/auth/login')) {
        sessionStorage.clear();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
