import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { LoginResponse } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly session = signal<LoginResponse | null>(this.load());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  login(username: string, password: string) {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { username, password })
      .pipe(
        tap((res) => {
          sessionStorage.setItem('token', res.token);
          sessionStorage.setItem('rol', res.rol);
          sessionStorage.setItem('restauranteId', String(res.restauranteId));
          sessionStorage.setItem('nombreRestaurante', res.nombreRestaurante);
          sessionStorage.setItem('modoServicioDefault', res.modoServicioDefault);
          this.session.set(res);
        })
      );
  }

  logout(): void {
    sessionStorage.clear();
    this.session.set(null);
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!this.session()?.token;
  }

  hasRole(...roles: string[]): boolean {
    const rol = this.session()?.rol;
    return !!rol && roles.includes(rol);
  }

  private load(): LoginResponse | null {
    const token = sessionStorage.getItem('token');
    const rol = sessionStorage.getItem('rol');
    const restauranteId = sessionStorage.getItem('restauranteId');
    if (!token || !rol || !restauranteId) return null;
    return {
      token,
      rol,
      restauranteId: Number(restauranteId),
      nombreRestaurante: sessionStorage.getItem('nombreRestaurante') ?? '',
      modoServicioDefault: sessionStorage.getItem('modoServicioDefault') ?? 'A'
    };
  }
}
