import { Component, Input, OnChanges, OnInit, inject, signal } from '@angular/core';
import { QrCodeService } from '../../core/qr-code.service';
import { urlLocalQr } from '../../core/qr-url.util';

@Component({
  selector: 'app-mesa-qr',
  standalone: true,
  templateUrl: './mesa-qr.component.html',
  styleUrl: './mesa-qr.component.css'
})
export class MesaQrComponent implements OnInit, OnChanges {
  @Input() compact = false;

  readonly dataUrl = signal('');
  readonly url = signal('');
  readonly error = signal('');

  private readonly qr = inject(QrCodeService);

  ngOnInit(): void {
    void this.actualizar();
  }

  ngOnChanges(): void {
    void this.actualizar();
  }

  private async actualizar(): Promise<void> {
    const link = urlLocalQr();
    this.url.set(link);
    try {
      this.error.set('');
      this.dataUrl.set(await this.qr.generarDataUrl(link, this.compact ? 140 : 220));
    } catch (e) {
      this.dataUrl.set('');
      this.error.set('No se pudo generar el código QR');
      console.error('QR generation failed', e);
    }
  }

  abrirEnlace(): void {
    window.open(this.url(), '_blank');
  }
}
