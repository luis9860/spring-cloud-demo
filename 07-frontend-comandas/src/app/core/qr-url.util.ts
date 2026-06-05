/** Token QR único del local (todas las mesas). */
export const QR_LOCAL_TOKEN = 'comandas-local';

/** URL pública: un solo QR para todo el restaurante. */
export function urlLocalQr(): string {
  if (typeof window !== 'undefined' && window.location?.origin) {
    return `${window.location.origin}/qr`;
  }
  return '/qr';
}

/** @deprecated QR por mesa; usar urlLocalQr(). */
export function urlMesaQr(_qrToken: string): string {
  return urlLocalQr();
}
