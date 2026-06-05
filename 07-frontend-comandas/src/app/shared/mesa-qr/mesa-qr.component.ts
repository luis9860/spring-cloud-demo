import { Component, Input, OnChanges, inject, signal } from '@angular/core';
import { QrCodeService } from '../../core/qr-code.service';
import { urlLocalQr } from '../../core/qr-url.util';

@Component({
  selector: 'app-mesa-qr',
  standalone: true,
  templateUrl: './mesa-qr.component.html',
  styleUrl: './mesa-qr.component.css'
})
export class MesaQrComponent implements OnChanges {
  @Input() compact = false;

  readonly dataUrl = signal('');
  readonly url = signal('');

  private readonly qr = inject(QrCodeService);

  ngOnChanges(): void {
    this.actualizar();
  }

  private async actualizar(): Promise<void> {
    const link = urlLocalQr();
    this.url.set(link);
    try {
      this.dataUrl.set(await this.qr.generarDataUrl(link, this.compact ? 140 : 220));
    } catch {
      this.dataUrl.set('');
    }
  }

  abrirEnlace(): void {
    window.open(this.url(), '_blank');
  }
}
