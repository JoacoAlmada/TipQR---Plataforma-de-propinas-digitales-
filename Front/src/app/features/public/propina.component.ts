import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PropinaPublicaService } from '../../core/services/propina-publica.service';
import { QrDestino, OrdenEstado } from '../../core/models/propina.model';

type Estado = 'cargando' | 'elegir' | 'enviando' | 'confirmada' | 'error';

@Component({
  selector: 'app-propina-publica',
  standalone: true,
  imports: [],
  templateUrl: './propina.component.html'
})
export class PropinaPublicaComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(PropinaPublicaService);

  readonly montosSugeridos = [500, 1000, 1500, 2000];

  estado = signal<Estado>('cargando');
  destino = signal<QrDestino | null>(null);
  orden = signal<OrdenEstado | null>(null);
  errorMsg = signal('');

  montoSeleccionado = signal<number | null>(null);
  montoLibre = signal<string>('');

  private codigo = '';

  readonly montoFinal = computed<number>(() => {
    const libre = Number(this.montoLibre());
    if (libre > 0) return libre;
    return this.montoSeleccionado() ?? 0;
  });

  ngOnInit(): void {
    this.codigo = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!this.codigo) { this.fallar('Código QR inválido'); return; }
    this.service.resolverQr(this.codigo).subscribe({
      next: (d) => { this.destino.set(d); this.estado.set('elegir'); },
      error: () => this.fallar('Este código QR no es válido o ya no está activo.')
    });
  }

  elegirMonto(m: number): void {
    this.montoSeleccionado.set(m);
    this.montoLibre.set('');
  }

  onMontoLibre(value: string): void {
    this.montoLibre.set(value.replace(/[^0-9]/g, ''));
    if (this.montoLibre()) this.montoSeleccionado.set(null);
  }

  confirmar(): void {
    if (this.montoFinal() <= 0) { this.errorMsg.set('Elegí o ingresá un monto.'); return; }
    this.errorMsg.set('');
    this.estado.set('enviando');
    this.service.crearOrden(this.codigo, this.montoFinal()).subscribe({
      next: (o) => { this.orden.set(o); this.estado.set('confirmada'); },
      error: (err) => {
        this.estado.set('elegir');
        this.errorMsg.set(err?.error?.error ?? 'No se pudo registrar la propina. Intentá de nuevo.');
      }
    });
  }

  private fallar(msg: string): void {
    this.errorMsg.set(msg);
    this.estado.set('error');
  }
}
