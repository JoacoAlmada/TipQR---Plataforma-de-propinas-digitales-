import { Component, OnInit, inject, signal } from '@angular/core';
import { NotificacionService } from '../../core/services/notificacion.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion } from '../../core/models/notificacion.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [],
  templateUrl: './notificaciones.component.html'
})
export class NotificacionesComponent implements OnInit {
  private readonly notificacionService = inject(NotificacionService);
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  readonly puedeEnviar = this.auth.hasRole('DUENO') || this.auth.hasRole('ENCARGADO');

  notificaciones = signal<Notificacion[]>([]);
  sucursales = signal<Sucursal[]>([]);
  loading = signal(true);

  // Formulario de envío
  mostrarForm = signal(false);
  titulo = signal('');
  mensaje = signal('');
  categoria = signal<string | null>(null);
  sucursalId = signal<number | null>(null);
  enviando = signal(false);
  formMsg = signal('');

  // Agente de IA (redactar con IA)
  instruccionIa = signal('');
  redactando = signal(false);
  iaMsg = signal('');
  asistidoConIa = signal(false);

  ngOnInit(): void {
    this.cargar();
    if (this.puedeEnviar) {
      this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });
    }
  }

  cargar(): void {
    this.loading.set(true);
    this.notificacionService.mias().subscribe({
      next: (n) => { this.notificaciones.set(n); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  marcarLeida(n: Notificacion): void {
    if (n.leida) return;
    this.notificacionService.marcarLeida(n.id).subscribe({
      next: () => this.notificaciones.update(list =>
        list.map(x => x.id === n.id ? { ...x, leida: true } : x))
    });
  }

  /** Pide al agente que redacte un borrador; el usuario lo revisa/edita antes de enviar. */
  redactarIa(): void {
    if (!this.instruccionIa().trim()) {
      this.iaMsg.set('Escribí qué querés avisar.');
      return;
    }
    this.iaMsg.set('');
    this.redactando.set(true);
    this.notificacionService.redactarIa(this.instruccionIa()).subscribe({
      next: (r) => {
        this.redactando.set(false);
        this.titulo.set(r.titulo);
        this.mensaje.set(r.mensaje);
        this.categoria.set(r.categoria);
        this.asistidoConIa.set(true);
        this.iaMsg.set(r.generadoPorIa
          ? 'Borrador generado con IA. Revisalo y editalo antes de enviar.'
          : 'Borrador armado automáticamente. Revisalo y editalo antes de enviar.');
      },
      error: (err) => {
        this.redactando.set(false);
        this.iaMsg.set(err?.error?.error ?? 'No se pudo generar el borrador.');
      }
    });
  }

  enviar(): void {
    if (!this.titulo().trim() || !this.mensaje().trim()) {
      this.formMsg.set('Completá el título y el mensaje.');
      return;
    }
    this.formMsg.set('');
    this.enviando.set(true);
    this.notificacionService.enviar({
      titulo: this.titulo(), mensaje: this.mensaje(), categoria: this.categoria(),
      sucursalId: this.sucursalId(), asistidoPorIa: this.asistidoConIa()
    }).subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.formMsg.set(`Enviada a ${r.enviados} empleado(s).`);
        this.resetForm();
        setTimeout(() => { this.mostrarForm.set(false); this.formMsg.set(''); }, 1500);
      },
      error: (err) => {
        this.enviando.set(false);
        this.formMsg.set(err?.error?.error ?? 'No se pudo enviar.');
      }
    });
  }

  private resetForm(): void {
    this.titulo.set(''); this.mensaje.set(''); this.categoria.set(null);
    this.sucursalId.set(null); this.instruccionIa.set('');
    this.iaMsg.set(''); this.asistidoConIa.set(false);
  }

  icono(categoria: string | null): string {
    return categoria === 'PAGOS' ? '💸' : categoria === 'HORARIO' ? '🕒'
      : categoria === 'STOCK' ? '📦' : categoria === 'OPERATIVA' ? '⚙️' : '🔔';
  }
}
