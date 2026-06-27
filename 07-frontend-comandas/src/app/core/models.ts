export interface LoginResponse {
  token: string;
  rol: string;
  restauranteId: number;
  nombreRestaurante: string;
  modoServicioDefault: string;
}

export interface Silla {
  id: number;
  numero: number;
  codigo: string;
}

export interface Mesa {
  id: number;
  numero: number;
  codigo: string;
  capacidadSillas: number;
  estado: string;
  qrToken: string;
  sillas?: Silla[];
}

export interface LineaPedido {
  id: number;
  producto: string;
  cantidad: number;
  estado: string;
  silla: number;
  codigoSilla?: string;
  notas?: string;
}

export interface ComandaResumen {
  id: number;
  estado: string;
  numeroTurno: number;
  lineas: LineaPedido[];
}

export interface SesionActiva {
  sesionId: number;
  mesaNumero: number;
  mesaCodigo: string;
  comandas: ComandaResumen[];
}

export interface Producto {
  id: number;
  nombre: string;
  precio: number;
  categoriaId?: number;
  estacion: string;
}

export interface Usuario {
  id: number;
  username: string;
  rol: string;
  activo: boolean;
}

export interface LineaPendiente {
  lineaId: number;
  comandaId: number;
  mesa: number;
  mesaCodigo?: string;
  producto: string;
  cantidad: number;
  estado: string;
  estacion: string;
  notas: string;
}

export type TipoNotificacionMozo = 'RECIBIDO' | 'PREPARANDO' | 'LISTO';

export interface NotificacionMozo {
  tipo?: TipoNotificacionMozo;
  comandaId: number;
  mesaId?: number;
  mesa: number;
  mesaCodigo?: string;
  mensaje: string;
  productos?: string[];
  modoServicio: string;
}

export interface QrMesaInfo {
  qrToken: string;
  mesa: number;
  mesaCodigo: string;
  restaurante: string;
  sesionActiva: boolean;
  sillas: { id: number; numero: number; codigo: string }[];
}

export interface QrLineaComensal {
  esTuyo: boolean;
  etiquetaComensal: string;
  producto: string;
  estado: string;
  estadoLabel: string;
  cantidad?: number;
  codigoSilla: string;
  enCola?: boolean;
  posicionCola?: number;
}

export interface QrColaLinea {
  mesaNumero: number;
  mesaCodigo: string;
  codigoSilla: string;
  etiquetaComensal: string;
  producto: string;
  estado: string;
  estadoLabel: string;
  cantidad?: number;
  enCola?: boolean;
  posicionCola?: number;
}

export interface QrColaMesa {
  mesaNumero: number;
  mesaCodigo: string;
  lineas: QrColaLinea[];
}

export interface QrColaLocal {
  restaurante: string;
  mesas: QrColaMesa[];
  cola: QrColaLinea[];
}

export interface QrMesaResumen {
  id: number;
  numero: number;
  codigo: string;
  estado: string;
  sesionActiva: boolean;
  sillas: { id: number; numero: number; codigo: string }[];
}

export interface QrLocalInfo {
  qrToken: string;
  restaurante: string;
  mesas: QrMesaResumen[];
}

export interface QrPedido {
  mesa: number;
  mesaCodigo?: string;
  restaurante: string;
  comandaEstado: string;
  miSilla?: string;
  miNombre?: string;
  miApellido?: string;
  lineas: QrLineaComensal[];
}
