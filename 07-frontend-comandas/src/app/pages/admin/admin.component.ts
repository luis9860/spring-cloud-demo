import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { ComandasApiService } from '../../core/comandas-api.service';
import { Mesa } from '../../core/models';
import { MesaQrComponent } from '../../shared/mesa-qr/mesa-qr.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [FormsModule, RouterLink, MesaQrComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {
  mesas = signal<Mesa[]>([]);
  mesaSel = signal<Mesa | null>(null);
  mensaje = signal('');
  error = signal('');

  mesaCapacidad = 4;
  mesaCodigo = '';
  editCodigo = '';
  editCapacidad = 4;

  constructor(
    readonly auth: AuthService,
    private readonly api: ComandasApiService
  ) {}

  ngOnInit(): void {
    if (!this.auth.hasRole('ADMIN')) {
      this.error.set('Debe iniciar sesión como admin (admin / admin123)');
    }
    this.cargar();
  }

  cargar(): void {
    this.api.getMesas().subscribe({
      next: (m) => {
        this.mesas.set(m);
        const sel = this.mesaSel();
        if (sel) {
          const act = m.find((x) => x.id === sel.id);
          if (act) this.seleccionar(act);
        }
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al cargar mesas'))
    });
  }

  seleccionar(m: Mesa): void {
    this.mesaSel.set(m);
    this.editCodigo = m.codigo;
    this.editCapacidad = m.capacidadSillas;
    this.error.set('');
  }

  crearMesa(): void {
    this.api.crearMesa({
      capacidadSillas: this.mesaCapacidad,
      codigo: this.mesaCodigo || undefined
    }).subscribe({
      next: (r) => {
        const n = r.sillas?.length ?? r.capacidadSillas;
        this.mensaje.set(`Mesa #${r.numero} creada (${r.codigo}) con ${n} silla(s)`);
        this.mesaCodigo = '';
        this.cargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'No se pudo crear la mesa'))
    });
  }

  guardarMesa(): void {
    const m = this.mesaSel();
    if (!m) return;
    this.api.actualizarMesa(m.id, {
      codigo: this.editCodigo,
      capacidadSillas: this.editCapacidad
    }).subscribe({
      next: () => {
        this.mensaje.set(`Mesa #${m.numero} actualizada`);
        this.cargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al actualizar'))
    });
  }

  eliminarMesa(m: Mesa): void {
    if (!confirm(`¿Eliminar mesa #${m.numero} (${m.codigo}) y sus sillas?`)) return;
    this.api.eliminarMesa(m.id).subscribe({
      next: () => {
        this.mensaje.set(`Mesa #${m.numero} eliminada`);
        if (this.mesaSel()?.id === m.id) this.mesaSel.set(null);
        this.cargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al eliminar mesa'))
    });
  }

  sillasCompletas(m: Mesa): boolean {
    return (m.sillas?.length ?? 0) >= m.capacidadSillas;
  }

  agregarSilla(): void {
    const m = this.mesaSel();
    if (!m) {
      this.error.set('Seleccione una mesa');
      return;
    }
    if (this.sillasCompletas(m)) {
      this.error.set(
        `La mesa ya tiene ${m.capacidadSillas} sillas (capacidad máxima). Suba «Capacidad sillas» y pulse Guardar cambios.`
      );
      return;
    }
    this.error.set('');
    this.api.crearSilla(m.id).subscribe({
      next: (s) => {
        this.mensaje.set(`Silla ${s.codigo} agregada a mesa #${s.mesaNumero}`);
        this.cargar();
        const act = this.mesas().find((x) => x.id === m.id);
        if (act) this.seleccionar(act);
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al crear silla'))
    });
  }

  eliminarSilla(sillaId: number): void {
    if (!confirm('¿Eliminar esta silla?')) return;
    this.api.eliminarSilla(sillaId).subscribe({
      next: () => {
        this.mensaje.set('Silla eliminada');
        this.cargar();
        const m = this.mesaSel();
        if (m) {
          const act = this.mesas().find((x) => x.id === m.id);
          if (act) this.seleccionar(act);
        }
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al eliminar silla'))
    });
  }

  private leerError(e: unknown, fallback: string): string {
    const err = e as {
      error?: { message?: string; error?: string } | string;
      message?: string;
      status?: number;
    };
    const body = err?.error;
    if (typeof body === 'string' && body) return body;
    if (body && typeof body === 'object' && body.message) return body.message;
    if (err?.status === 403) return 'Solo el rol ADMIN puede administrar mesas';
    if (err?.status === 400) {
      return 'Capacidad máxima de sillas alcanzada. Aumente la capacidad y pulse Guardar cambios.';
    }
    if (err?.status === 409) return 'Conflicto: codigo o mesa en uso. Actualice el listado.';
    if (err?.status === 0) {
      return 'Backend no disponible. Levante Eureka, pedido-service (8082) y Gateway (8080), luego recargue (F5).';
    }
    return fallback;
  }
}
