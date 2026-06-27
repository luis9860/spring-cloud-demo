import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  error = signal('');
  loading = signal(false);

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {
    if (auth.isLoggedIn()) this.goHome();
  }

  usarDemo(user: string, pass: string): void {
    this.username = user;
    this.password = pass;
    this.submit();
  }

  submit(): void {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.username, this.password).subscribe({
      next: () => {
        this.loading.set(false);
        this.goHome();
      },
      error: (e) => {
        this.loading.set(false);
        const status = e?.status ?? 0;
        if (status === 401) {
          this.error.set('Usuario o contraseña incorrectos');
        } else if (status === 0) {
          this.error.set('No se pudo conectar al servidor. Espere unos segundos y recargue (F5).');
        } else {
          this.error.set(e?.error?.message ?? `Error del servidor (${status}). Intente de nuevo.`);
        }
      }
    });
  }

  private goHome(): void {
    const rol = this.auth.session()?.rol;
    if (rol === 'ADMIN') this.router.navigate(['/admin']);
    else if (rol === 'COCINERO') this.router.navigate(['/cocina']);
    else this.router.navigate(['/mozo']);
  }
}
