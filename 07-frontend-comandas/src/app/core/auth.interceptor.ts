import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const restauranteId = sessionStorage.getItem('restauranteId');
  const rol = sessionStorage.getItem('rol');
  const token = sessionStorage.getItem('token');

  let headers = req.headers;
  if (restauranteId) headers = headers.set('X-Restaurante-Id', restauranteId);
  if (rol) headers = headers.set('X-Rol', rol);
  if (token) headers = headers.set('Authorization', `Bearer ${token}`);

  return next(req.clone({ headers }));
};
