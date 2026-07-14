import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PropinaPublicaService } from '../../core/services/propina-publica.service';
import { OrdenEstado } from '../../core/models/propina.model';

type Vista = 'consultando' | 'pagada' | 'rechazada' | 'pendiente' | 'error';

@Component({
  selector: 'app-pago-resultado',
  standalone: true,
  imports: [],
  templateUrl: './pago-resultado.component.html'
})
export class PagoResultadoComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(PropinaPublicaService);

  vista = signal<Vista>('consultando');
  orden = signal<OrdenEstado | null>(null);

  private intentos = 0;
  private readonly maxIntentos = 15;
  private timer: ReturnType<typeof setTimeout> | null = null;
  private ordenCodigo = '';

  ngOnInit(): void {
    this.ordenCodigo = this.route.snapshot.queryParamMap.get('orden') ?? '';
    if (!this.ordenCodigo) { this.vista.set('error'); return; }
    this.consultar();
  }

  ngOnDestroy(): void {
    if (this.timer) clearTimeout(this.timer);
  }

  private consultar(): void {
    this.service.estadoOrden(this.ordenCodigo).subscribe({
      next: (o) => {
        this.orden.set(o);
        switch (o.estado) {
          case 'PAGADA': this.vista.set('pagada'); break;
          case 'RECHAZADA':
          case 'CANCELADA':
          case 'EXPIRADA': this.vista.set('rechazada'); break;
          default: this.reintentarOPendiente();
        }
      },
      error: () => this.reintentarOError()
    });
  }

  /** El webhook puede tardar unos segundos; reintentamos antes de mostrar "pendiente". */
  private reintentarOPendiente(): void {
    if (++this.intentos >= this.maxIntentos) { this.vista.set('pendiente'); return; }
    this.timer = setTimeout(() => this.consultar(), 2000);
  }

  private reintentarOError(): void {
    if (++this.intentos >= this.maxIntentos) { this.vista.set('error'); return; }
    this.timer = setTimeout(() => this.consultar(), 2000);
  }
}
