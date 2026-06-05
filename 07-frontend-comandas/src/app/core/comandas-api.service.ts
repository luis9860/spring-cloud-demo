import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import {
  LineaPendiente,
  Mesa,
  SesionActiva,
  NotificacionMozo,
  Producto,
  QrColaLocal,
  QrLocalInfo,
  QrMesaInfo,
  QrPedido,
  Silla
} from './models';

@Injectable({ providedIn: 'root' })
export class ComandasApiService {
  private readonly base = environment.apiUrl;

  constructor(private readonly http: HttpClient) {}

  getMesas() {
    return this.http.get<Mesa[]>(`${this.base}/mesas`);
  }

  crearMesa(body: { capacidadSillas: number; codigo?: string }) {
    return this.http.post<Mesa>(`${this.base}/mesas`, {
      capacidadSillas: body.capacidadSillas,
      codigo: body.codigo ?? null
    });
  }

  actualizarMesa(mesaId: number, body: { codigo?: string; capacidadSillas?: number }) {
    return this.http.put<Mesa>(`${this.base}/mesas/${mesaId}`, body);
  }

  eliminarMesa(mesaId: number) {
    return this.http.delete<void>(`${this.base}/mesas/${mesaId}`);
  }

  crearSilla(mesaId: number) {
    return this.http.post<Silla & { mesaCodigo: string; mesaNumero: number }>(
      `${this.base}/mesas/${mesaId}/sillas`,
      {}
    );
  }

  eliminarSilla(sillaId: number) {
    return this.http.delete<void>(`${this.base}/mesas/sillas/${sillaId}`);
  }

  getSillas(mesaId: number) {
    return this.http.get<Silla[]>(`${this.base}/mesas/${mesaId}/sillas`);
  }

  abrirMesa(mesaId: number) {
    return this.http.post<{ comandaId: number; sesionId: number; mesaId: number }>(
      `${this.base}/mesas/${mesaId}/abrir`,
      {}
    );
  }

  getSesionActiva(mesaId: number) {
    return this.http.get<SesionActiva>(`${this.base}/mesas/${mesaId}/sesion-activa`);
  }

  getProductos(estacion?: string) {
    const q = estacion ? `?estacion=${estacion}` : '';
    return this.http.get<Producto[]>(`${this.base}/productos${q}`);
  }

  agregarLinea(mesaId: number, silla: number, body: { productoId: number; cantidad: number; notas?: string }) {
    return this.http.post<{ lineaId: number; comandaId: number }>(
      `${this.base}/mesas/${mesaId}/sillas/${silla}/lineas`,
      body
    );
  }

  enviarComanda(comandaId: number) {
    return this.http.post<{ estado: string }>(`${this.base}/comandas/${comandaId}/enviar`, {});
  }

  aceptarComandaCocina(comandaId: number) {
    return this.http.post<{ comandaId: number; estado: string }>(
      `${this.base}/comandas/${comandaId}/aceptar-cocina`,
      {}
    );
  }

  marcarComandaEntregada(comandaId: number) {
    return this.http.patch<{
      comandaId: number;
      comandaEstado: string;
      platosServidos: number;
      platosPendientes: number;
    }>(`${this.base}/comandas/${comandaId}/entregada`, {});
  }

  cerrarMesa(mesaId: number) {
    return this.http.post<void>(`${this.base}/mesas/${mesaId}/cerrar`, {});
  }

  getPendientesCocina(estacion = 'COCINA') {
    return this.http.get<LineaPendiente[]>(`${this.base}/comandas/pendientes`, {
      params: { estacion }
    });
  }

  marcarLineaLista(lineaId: number) {
    return this.http.patch<{ estado: string; comandaEstado: string }>(
      `${this.base}/lineas/${lineaId}/estado`,
      { estado: 'LISTA' }
    );
  }

  anularLinea(lineaId: number) {
    return this.http.patch<{ lineaId: number; estado: string }>(
      `${this.base}/lineas/${lineaId}/estado`,
      { estado: 'ANULADA' }
    );
  }

  getNotificacionesMozo() {
    return this.http.get<NotificacionMozo[]>(`${this.base}/mozo/notificaciones`);
  }

  getPlatosListosMozo() {
    return this.http.get<NotificacionMozo[]>(`${this.base}/mozo/platos-listos`);
  }

  getLocalQr() {
    return this.http.get<QrLocalInfo>(`${this.base}/publico/local`);
  }

  getColaLocal() {
    return this.http.get<QrColaLocal>(`${this.base}/publico/local/cola`);
  }

  getMesaQr(qrToken: string) {
    return this.http.get<QrMesaInfo>(`${this.base}/publico/mesa/${qrToken}`);
  }

  identificarComensalQr(
    qrToken: string,
    body: { mesaCodigo: string; codigoSilla: string; abrirMesaSiLibre?: boolean }
  ) {
    return this.http.post<{ codigoSilla: string; mesaCodigo: string; mesaNumero: number }>(
      `${this.base}/publico/pedido/${qrToken}/identificar`,
      body
    );
  }

  getPedidoQr(qrToken: string, mesaCodigo: string, miSilla: string) {
    return this.http.get<QrPedido>(`${this.base}/publico/pedido/${qrToken}`, {
      params: { mesaCodigo, miSilla }
    });
  }

  getMenuQr(qrToken: string, mesaCodigo: string) {
    return this.http.get<{ mesaCodigo: string; productos: Producto[] }>(
      `${this.base}/publico/pedido/${qrToken}/menu`,
      { params: { mesaCodigo } }
    );
  }

  agregarLineaQr(
    qrToken: string,
    mesaCodigo: string,
    miSilla: string,
    body: { productoId: number; cantidad: number; notas?: string }
  ) {
    return this.http.post<{ lineaId: number; comandaId: number; producto: string }>(
      `${this.base}/publico/pedido/${qrToken}/lineas`,
      body,
      { params: { mesaCodigo, miSilla } }
    );
  }

  enviarPedidoQr(qrToken: string, mesaCodigo: string, miSilla: string) {
    return this.http.post<{ comandaId: number; estado: string }>(
      `${this.base}/publico/pedido/${qrToken}/enviar`,
      {},
      { params: { mesaCodigo, miSilla } }
    );
  }
}
