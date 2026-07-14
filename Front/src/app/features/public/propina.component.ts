import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PropinaPublicaService } from '../../core/services/propina-publica.service';
import { QrDestino, OrdenEstado, MesaDestinatarios } from '../../core/models/propina.model';

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
  destinatarios = signal<MesaDestinatarios | null>(null);
  orden = signal<OrdenEstado | null>(null);
  errorMsg = signal('');

  montoSeleccionado = signal<number | null>(null);
  montoLibre = signal<string>('');

  // Solo para mesa: elegir a quién va la propina.
  modoMesa = signal<'mozo' | 'equipo' | null>(null);
  mozoElegido = signal<number | null>(null);

  private codigo = '';

  readonly esMesa = computed(() => this.destino()?.tipoDestino === 'MESA');

  readonly montoFinal = computed<number>(() => {
    const libre = Number(this.montoLibre());
    if (libre > 0) return libre;
    return this.montoSeleccionado() ?? 0;
  });

  /** Para mesa hay que elegir destinatario; para empleado/grupo no aplica. */
  readonly destinatarioListo = computed<boolean>(() => {
    if (!this.esMesa()) return true;
    if (this.modoMesa() === 'equipo') return true;
    return this.modoMesa() === 'mozo' && this.mozoElegido() != null;
  });

  ngOnInit(): void {
    this.codigo = this.route.snapshot.paramMap.get('codigo') ?? '';
    if (!this.codigo) { this.fallar('Código QR inválido'); return; }
    this.service.resolverQr(this.codigo).subscribe({
      next: (d) => {
        this.destino.set(d);
        this.estado.set('elegir');
        if (d.tipoDestino === 'MESA') {
          this.service.destinatariosMesa(this.codigo).subscribe({
            next: (m) => this.destinatarios.set(m)
          });
        }
      },
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

  elegirEquipo(): void {
    this.modoMesa.set('equipo');
    this.mozoElegido.set(null);
  }

  elegirMozo(empleadoId: number): void {
    this.modoMesa.set('mozo');
    this.mozoElegido.set(empleadoId);
  }

  confirmar(): void {
    if (this.montoFinal() <= 0) { this.errorMsg.set('Elegí o ingresá un monto.'); return; }
    if (!this.destinatarioListo()) { this.errorMsg.set('Elegí a quién dejarle la propina.'); return; }
    this.errorMsg.set('');
    this.estado.set('enviando');
    const empleadoId = this.esMesa() && this.modoMesa() === 'mozo' ? this.mozoElegido() : null;
    this.service.crearOrden(this.codigo, this.montoFinal(), empleadoId).subscribe({
      next: (o) => {
        this.orden.set(o);
        this.service.iniciarPago(o.codigo).subscribe({
          next: (p) => { window.location.href = p.checkoutUrl; },
          error: (err) => {
            this.estado.set('elegir');
            this.errorMsg.set(err?.error?.error ?? 'No se pudo iniciar el pago. Intentá de nuevo.');
          }
        });
      },
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
