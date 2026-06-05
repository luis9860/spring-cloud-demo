import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ComandasApiService } from '../../core/comandas-api.service';
import { QrColaLocal } from '../../core/models';
import { environment } from '../../../environments/environment';

type VistaQr = 'inicio' | 'app' | 'pedido';

@Component({
  selector: 'app-qr',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './qr.component.html',
  styleUrl: './qr.component.css'
})
export class QrComponent implements OnInit, OnDestroy {
  readonly appInfo = environment.comensalApp;

  cola = signal<QrColaLocal | null>(null);
  vista = signal<VistaQr>('inicio');
  error = signal('');

  private timer?: ReturnType<typeof setInterval>;

  constructor(private readonly api: ComandasApiService) {}

  ngOnInit(): void {
    this.timer = setInterval(() => {
      if (this.vista() === 'pedido') {
        this.cargarCola();
      }
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.timer) clearInterval(this.timer);
  }

  irApp(): void {
    this.error.set('');
    this.vista.set('app');
  }

  irPedido(): void {
    this.error.set('');
    this.vista.set('pedido');
    this.cargarCola();
  }

  volver(): void {
    this.vista.set('inicio');
    this.cola.set(null);
    this.error.set('');
  }

  cargarCola(): void {
    this.api.getColaLocal().subscribe({
      next: (c) => {
        this.cola.set(c);
        this.error.set('');
      },
      error: () => this.error.set('No se pudo cargar la cola de pedidos')
    });
  }
}
