import { Injectable } from '@angular/core';
import QRCode from 'qrcode';

@Injectable({ providedIn: 'root' })
export class QrCodeService {
  generarDataUrl(texto: string, size = 200): Promise<string> {
    return QRCode.toDataURL(texto, {
      width: size,
      margin: 1,
      errorCorrectionLevel: 'M'
    });
  }
}
