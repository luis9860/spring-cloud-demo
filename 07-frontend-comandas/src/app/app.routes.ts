import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { MozoComponent } from './pages/mozo/mozo.component';
import { CocinaComponent } from './pages/cocina/cocina.component';
import { QrComponent } from './pages/qr/qr.component';
import { AdminComponent } from './pages/admin/admin.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'admin', component: AdminComponent, canActivate: [authGuard, roleGuard('ADMIN')] },
  { path: 'mozo', component: MozoComponent, canActivate: [authGuard, roleGuard('MOZO', 'ADMIN')] },
  { path: 'cocina', component: CocinaComponent, canActivate: [authGuard, roleGuard('COCINERO', 'ADMIN')] },
  { path: 'qr', component: QrComponent },
  { path: 'qr/:token', redirectTo: 'qr', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];
