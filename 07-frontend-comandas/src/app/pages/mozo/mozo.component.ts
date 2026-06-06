import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ComandasApiService } from '../../core/comandas-api.service';
import { AuthService } from '../../core/auth.service';
import { LineaPedido, Mesa, NotificacionMozo, Producto } from '../../core/models';
import { MesaQrComponent } from '../../shared/mesa-qr/mesa-qr.component';

@Component({
  selector: 'app-mozo',
  standalone: true,
  imports: [FormsModule, RouterLink, MesaQrComponent],
  templateUrl: './mozo.component.html',
  styleUrl: './mozo.component.css'
})
export class MozoComponent implements OnInit, OnDestroy {
  private notifTimer?: ReturnType<typeof setInterval>;
  mesas = signal<Mesa[]>([]);
  productos = signal<Producto[]>([]);
  notificaciones = signal<NotificacionMozo[]>([]);
  platosListos = signal<NotificacionMozo[]>([]);
  notifCocina = computed(() =>
    this.notificaciones().filter((n) => n.tipo === 'RECIBIDO' || n.tipo === 'PREPARANDO')
  );
  notifListos = computed(() => {
    const porComanda = new Map<number, NotificacionMozo>();
    for (const n of [...this.platosListos(), ...this.notificaciones()]) {
      if (n.tipo === 'LISTO' && !porComanda.has(n.comandaId)) {
        porComanda.set(n.comandaId, n);
      }
    }
    return [...porComanda.values()];
  });
  lineasListasEnMesa = computed(() =>
    this.lineasPedido().filter((l) => l.estado === 'LISTA')
  );
  lineasPedido = signal<LineaPedido[]>([]);
  mensaje = signal('');
  error = signal('');

  mesaSeleccionada = signal<Mesa | null>(null);
  comandaId = signal<number | null>(null);
  modalMesas = signal(false);
  buscarMesa = '';
  qrVisible = signal(false);

  mesasFiltradas = computed(() => {
    const q = this.buscarMesa.trim().toLowerCase();
    const list = this.mesas();
    if (!q) return list;
    return list.filter(
      (m) =>
        m.codigo.toLowerCase().includes(q) ||
        String(m.numero).includes(q) ||
        (m.estado ?? '').toLowerCase().includes(q)
    );
  });

  productoId = 1;
  cantidad = 1;
  silla = 1;
  notas = '';

  constructor(
    readonly auth: AuthService,
    private readonly api: ComandasApiService
  ) {}

  private primeraCarga = true;

  ngOnInit(): void {
    this.recargar();
    this.cargarNotificaciones();
    this.notifTimer = setInterval(() => {
      this.cargarNotificaciones();
      const m = this.mesaSeleccionada();
      if (m?.estado === 'OCUPADA') {
        this.cargarPedido(m.id);
      }
    }, 4000);
  }

  ngOnDestroy(): void {
    if (this.notifTimer) {
      clearInterval(this.notifTimer);
    }
  }

  recargar(): void {
    this.api.getMesas().subscribe({
      next: (m) => {
        this.mesas.set(m);
        const sel = this.mesaSeleccionada();
        if (sel) {
          const act = m.find((x) => x.id === sel.id);
          if (act) {
            this.seleccionarMesa(act, false);
          }
        } else if (this.primeraCarga && m.length) {
          this.modalMesas.set(true);
        }
        this.primeraCarga = false;
      },
      error: () => this.error.set('No se pudo cargar mesas. ¿Backend y Gateway activos?')
    });
    this.api.getProductos().subscribe({
      next: (p) => {
        this.productos.set(p);
        if (p.length && !p.some((x) => x.id === this.productoId)) {
          this.productoId = p[0].id;
        }
      },
      error: () => {}
    });
    this.cargarNotificaciones();
  }

  cargarNotificaciones(): void {
    this.api.getNotificacionesMozo().subscribe({
      next: (n) => this.notificaciones.set(Array.isArray(n) ? n : []),
      error: (e) => this.error.set(this.leerError(e, 'No se pudieron cargar avisos de cocina'))
    });
    this.api.getPlatosListosMozo().subscribe({
      next: (n) => this.platosListos.set(Array.isArray(n) ? n : []),
      error: (e) => {
        const msg = this.leerError(e, 'No se pudieron cargar platos listos');
        if (msg.includes('404')) {
          this.error.set(
            'Reinicie pedido-service (puerto 8082) para aplicar la actualización del mozo.'
          );
        } else {
          this.error.set(msg);
        }
      }
    });
  }

  cantidadSillas(m: Mesa): number {
    return m.sillas?.length ?? m.capacidadSillas;
  }

  codigoSillaActiva(): string {
    const m = this.mesaSeleccionada();
    if (!m?.sillas?.length) return '';
    const s = m.sillas.find((x) => x.numero === this.silla);
    return s?.codigo ?? '';
  }

  codigoSillaLinea(linea: LineaPedido): string {
    if (linea.codigoSilla) return linea.codigoSilla;
    const m = this.mesaSeleccionada();
    const s = m?.sillas?.find((x) => x.numero === linea.silla);
    return s?.codigo ?? `Silla ${linea.silla}`;
  }

  abrirModalMesas(): void {
    this.modalMesas.set(true);
  }

  cerrarModalMesas(): void {
    this.modalMesas.set(false);
  }

  elegirMesaEnModal(m: Mesa): void {
    this.seleccionarMesa(m);
    this.cerrarModalMesas();
  }

  toggleQr(): void {
    this.qrVisible.update((v) => !v);
  }

  seleccionarMesa(m: Mesa, limpiarPedido = true): void {
    this.mesaSeleccionada.set(m);
    this.mensaje.set('');
    this.error.set('');
    if (m.sillas?.length) {
      this.silla = m.sillas[0].numero;
    }
    if (limpiarPedido) {
      this.comandaId.set(null);
      this.lineasPedido.set([]);
    }
    if (m.estado === 'OCUPADA') {
      this.cargarPedido(m.id);
    }
  }

  cargarPedido(mesaId: number): void {
    this.api.getSesionActiva(mesaId).subscribe({
      next: (s) => {
        const comandas = s.comandas ?? [];
        const todasLineas = comandas.flatMap((c) => c.lineas ?? []);
        this.lineasPedido.set(todasLineas);
        const borrador = comandas.find((c) => c.estado === 'BORRADOR');
        const conPendientes = comandas.filter((c) => c.estado !== 'ENTREGADA');
        const comandaTrabajo =
          borrador ?? conPendientes[conPendientes.length - 1] ?? comandas[comandas.length - 1];
        this.comandaId.set(comandaTrabajo?.id ?? null);
      },
      error: () => {
        this.lineasPedido.set([]);
      }
    });
  }

  abrirMesa(): void {
    const m = this.mesaSeleccionada();
    if (!m) return;
    this.api.abrirMesa(m.id).subscribe({
      next: (r) => {
        this.comandaId.set(r.comandaId);
        this.mensaje.set(`Mesa ${m.codigo} (#${m.numero}) abierta. Comanda #${r.comandaId}`);
        this.recargar();
        this.cargarPedido(m.id);
      },
      error: (e) => {
        const msg = e?.error?.message ?? '';
        if (msg.includes('ocupada') || msg.includes('Ocupada')) {
          this.cargarPedido(m.id);
          this.recargar();
          this.mensaje.set(`Mesa ${m.codigo} ya tiene pedido activo`);
        } else {
          this.error.set(msg || 'Error al abrir mesa');
        }
      }
    });
  }

  agregarLinea(): void {
    const m = this.mesaSeleccionada();
    if (!m) return;
    if (m.estado === 'LIBRE') {
      this.error.set('Primero abra la mesa');
      return;
    }
    this.api.agregarLinea(m.id, this.silla, {
      productoId: this.productoId,
      cantidad: this.cantidad,
      notas: this.notas || undefined
    }).subscribe({
      next: (r) => {
        this.comandaId.set(r.comandaId);
        const prod = this.productos().find((p) => p.id === this.productoId);
        this.mensaje.set(
          `Agregado: ${prod?.nombre ?? 'producto'} x${this.cantidad} (${this.codigoSillaActiva()})`
        );
        this.notas = '';
        this.cargarPedido(m.id);
      },
      error: (e) => this.error.set(e?.error?.message ?? 'Error al agregar línea')
    });
  }

  enviarComanda(): void {
    const id = this.comandaId();
    if (!id) {
      this.error.set('Abra la mesa y agregue al menos una línea');
      return;
    }
    if (this.lineasPedido().length === 0) {
      this.error.set('Agregue al menos una línea antes de enviar');
      return;
    }
    this.api.enviarComanda(id).subscribe({
      next: () => {
        this.mensaje.set(`Comanda #${id} enviada a cocina`);
        const m = this.mesaSeleccionada();
        if (m) {
          this.cargarPedido(m.id);
        }
        this.cargarNotificaciones();
        this.recargar();
      },
      error: (e) => this.error.set(e?.error?.message ?? 'Error al enviar')
    });
  }

  /** Etiquetas para el mozo (el código interno sigue siendo ENVIADA, LISTA, ENTREGADA…). */
  estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      BORRADOR: 'Tomando pedido',
      ENVIADA: 'Enviada a cocina',
      EN_PREPARACION: 'Preparando en cocina',
      PARCIALMENTE_LISTA: 'Platos listos (parcial)',
      LISTA: 'Listo en cocina',
      ENTREGADA: 'Servido al cliente',
      LIBRE: 'Mesa libre',
      OCUPADA: 'Clientes en mesa'
    };
    return map[estado] ?? estado;
  }

  productosTexto(n: NotificacionMozo): string {
    return (n.productos ?? []).join(', ') || '—';
  }

  marcarServidoAlCliente(n: NotificacionMozo): void {
    if (n.tipo && n.tipo !== 'LISTO') {
      this.error.set('Espere a que cocina marque los platos como listos');
      return;
    }
    const codigo = n.mesaCodigo ?? `Mesa #${n.mesa}`;
    const platos = this.productosTexto(n);
    if (
      !confirm(
        `¿Confirmar que llevó a ${codigo} solo los platos listos?\n\n${platos}\n\n` +
          `Los demás platos de la comanda #${n.comandaId} siguen en cocina.`
      )
    ) {
      return;
    }
    this.error.set('');
    this.api.marcarComandaEntregada(n.comandaId).subscribe({
      next: (r) => {
        const extra =
          r.platosPendientes > 0
            ? ` Quedan ${r.platosPendientes} plato(s) en cocina o por servir.`
            : ' Comanda completa servida.';
        this.mensaje.set(
          `${codigo}: ${r.platosServidos} plato(s) servidos al cliente.${extra} La mesa sigue OCUPADA.`
        );
        this.cargarNotificaciones();
        const mesaId = n.mesaId ?? this.mesas().find((m) => m.numero === n.mesa)?.id;
        if (mesaId) {
          const m = this.mesas().find((x) => x.id === mesaId);
          if (m) this.seleccionarMesa(m, false);
        }
      },
      error: (e) => this.error.set(e?.error?.message ?? 'No se pudo marcar como servido')
    });
  }

  liberarMesaSeleccionada(): void {
    const m = this.mesaSeleccionada();
    if (!m) {
      this.error.set('Seleccione una mesa');
      return;
    }
    if (m.estado !== 'OCUPADA') {
      this.error.set('La mesa ya está libre');
      return;
    }
    if (
      !confirm(
        `¿Los clientes de ${m.codigo} (#${m.numero}) ya se fueron?\n\n` +
          `Si hay platos en cocina o listos, primero use «Servido al cliente».\n` +
          `Las líneas en carrito (sin enviar) se quitarán solas.`
      )
    ) {
      return;
    }
    this.error.set('');
    this.api.cerrarMesa(m.id).subscribe({
      next: () => {
        this.mensaje.set(`Mesa ${m.codigo} liberada`);
        this.lineasPedido.set([]);
        this.comandaId.set(null);
        this.recargar();
      },
      error: (e) => this.error.set(this.leerError(e, 'Error al liberar mesa'))
    });
  }

  eliminarLinea(linea: LineaPedido): void {
    if (linea.estado !== 'BORRADOR') {
      this.error.set('Solo puede quitar líneas en carrito (antes de «Enviar a cocina»)');
      return;
    }
    if (!confirm(`¿Quitar ${linea.producto} de la comanda?`)) return;
    this.error.set('');
    this.api.anularLinea(linea.id).subscribe({
      next: () => {
        this.mensaje.set('Línea eliminada');
        const m = this.mesaSeleccionada();
        if (m) this.seleccionarMesa(m, false);
      },
      error: (e) => this.error.set(this.leerError(e, 'No se pudo eliminar la línea'))
    });
  }

  private leerError(e: unknown, fallback: string): string {
    const err = e as {
      error?: { message?: string } | string;
      status?: number;
    };
    const body = err?.error;
    if (typeof body === 'string' && body) return body;
    if (body && typeof body === 'object' && body.message) return body.message;
    if (err?.status === 409) return fallback + ' (conflicto: revise pedidos en cocina)';
    if (err?.status === 0) {
      return 'Backend no disponible. Verifique pedido-service (8082) y Gateway (8080).';
    }
    return fallback;
  }
}
