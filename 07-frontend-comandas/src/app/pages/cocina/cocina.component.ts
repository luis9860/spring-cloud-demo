import { Component, OnInit, computed, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ComandasApiService } from '../../core/comandas-api.service';
import { AuthService } from '../../core/auth.service';
import { LineaPendiente } from '../../core/models';

export interface GrupoComandaCocina {
  comandaId: number;
  mesaCodigo: string;
  mesa: number;
  lineas: LineaPendiente[];
  esNuevo: boolean;
}

@Component({
  selector: 'app-cocina',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './cocina.component.html',
  styleUrl: './cocina.component.css'
})
export class CocinaComponent implements OnInit {
  pendientes = signal<LineaPendiente[]>([]);
  mensaje = signal('');
  error = signal('');

  grupos = computed(() => this.agruparPorComanda(this.pendientes()));

  constructor(
    readonly auth: AuthService,
    private readonly api: ComandasApiService
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.api.getPendientesCocina('COCINA').subscribe({
      next: (list) => {
        const arr = Array.isArray(list) ? list : [list as unknown as LineaPendiente];
        this.pendientes.set(arr.filter((x) => x?.lineaId));
      },
      error: () => this.error.set('Error al cargar cola de cocina')
    });
  }

  private agruparPorComanda(lineas: LineaPendiente[]): GrupoComandaCocina[] {
    const map = new Map<number, GrupoComandaCocina>();
    for (const l of lineas) {
      let g = map.get(l.comandaId);
      if (!g) {
        g = {
          comandaId: l.comandaId,
          mesaCodigo: l.mesaCodigo ?? `Mesa ${l.mesa}`,
          mesa: l.mesa,
          lineas: [],
          esNuevo: false
        };
        map.set(l.comandaId, g);
      }
      g.lineas.push(l);
      if (l.estado === 'ENVIADA') {
        g.esNuevo = true;
      }
    }
    return [...map.values()].sort((a, b) => a.comandaId - b.comandaId);
  }

  estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      ENVIADA: 'Nuevo — pendiente aceptar',
      EN_PREPARACION: 'En preparación',
      LISTA: 'Listo'
    };
    return map[estado] ?? estado;
  }

  aceptarPedido(g: GrupoComandaCocina): void {
    this.error.set('');
    this.api.aceptarComandaCocina(g.comandaId).subscribe({
      next: () => {
        this.mensaje.set(`Comanda #${g.comandaId} (${g.mesaCodigo}) aceptada — en preparación`);
        this.cargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'No se pudo aceptar el pedido'))
    });
  }

  marcarLista(lineaId: number): void {
    this.api.marcarLineaLista(lineaId).subscribe({
      next: () => {
        this.mensaje.set(`Línea #${lineaId} lista para servir`);
        this.cargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al actualizar'))
    });
  }

  private leerError(e: unknown, fallback: string): string {
    const err = e as {
      error?: { message?: string; error?: string } | string;
      status?: number;
    };
    const body = err?.error;
    if (typeof body === 'string' && body) return body;
    if (body && typeof body === 'object' && body.message) return body.message;
    if (err?.status === 404) {
      return 'Endpoint no encontrado. Reinicie pedido-service (8082) con el código actualizado.';
    }
    if (err?.status === 0) {
      return 'Backend no disponible. Verifique Gateway (8080) y pedido-service (8082).';
    }
    return fallback;
  }
}
