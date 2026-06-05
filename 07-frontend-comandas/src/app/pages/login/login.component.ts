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
  username = 'mozo1';
  password = 'mozo123';
  error = signal('');
  loading = signal(false);

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {
    if (auth.isLoggedIn()) this.goHome();
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
        this.error.set(e?.error?.message ?? 'Credenciales invalidas o backend no disponible');
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
