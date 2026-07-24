import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { NotificacionService } from '../../core/services/notificacion.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { TurnoService } from '../../core/services/turno.service';
import { AuthService } from '../../core/services/auth.service';
import { Notificacion, NotificacionEnviada, CrearNotificacionRequest } from '../../core/models/notificacion.model';
import { Sucursal } from '../../core/models/sucursal.model';
import { Turno } from '../../core/models/turno.model';

type Destinatario = 'EMPRESA' | 'SUCURSAL' | 'TURNO' | 'ROL';

@Component({
  selector: 'app-notificaciones',
  standalone: true,
  imports: [],
  templateUrl: './notificaciones.component.html'
})
export class NotificacionesComponent implements OnInit {
  private readonly notificacionService = inject(NotificacionService);
  private readonly sucursalService = inject(SucursalService);
  private readonly turnoService = inject(TurnoService);
  private readonly auth = inject(AuthService);

  readonly puedeEnviar = this.auth.hasRole('DUENO') || this.auth.hasRole('ENCARGADO');

  notificaciones = signal<Notificacion[]>([]);
  enviadas = signal<NotificacionEnviada[]>([]);
  sucursales = signal<Sucursal[]>([]);
  turnos = signal<Turno[]>([]);
  loading = signal(true);

  // Solapa activa: Recibidas / Enviadas
  vista = signal<'recibidas' | 'enviadas'>('recibidas');

  // Búsqueda, filtro por fecha y orden
  busqueda = signal('');
  desde = signal('');
  hasta = signal('');
  orden = signal<'desc' | 'asc'>('desc');

  /** Bandeja con búsqueda de texto, filtro por rango de fechas y orden por fecha. */
  notificacionesFiltradas = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const d = this.desde() ? new Date(this.desde() + 'T00:00:00').getTime() : null;
    const h = this.hasta() ? new Date(this.hasta() + 'T23:59:59').getTime() : null;
    const asc = this.orden() === 'asc';
    return this.notificaciones()
      .filter(n => {
        if (q && !(`${n.titulo} ${n.mensaje}`.toLowerCase().includes(q))) return false;
        const t = n.fecha ? new Date(n.fecha).getTime() : 0;
        if (d != null && t < d) return false;
        if (h != null && t > h) return false;
        return true;
      })
      .sort((a, b) => {
        const ta = a.fecha ? new Date(a.fecha).getTime() : 0;
        const tb = b.fecha ? new Date(b.fecha).getTime() : 0;
        return asc ? ta - tb : tb - ta;
      });
  });

  /** Mismo criterio de búsqueda/fecha/orden aplicado a las enviadas. */
  enviadasFiltradas = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const d = this.desde() ? new Date(this.desde() + 'T00:00:00').getTime() : null;
    const h = this.hasta() ? new Date(this.hasta() + 'T23:59:59').getTime() : null;
    const asc = this.orden() === 'asc';
    return this.enviadas()
      .filter(n => {
        if (q && !(`${n.titulo} ${n.mensaje}`.toLowerCase().includes(q))) return false;
        const t = n.fecha ? new Date(n.fecha).getTime() : 0;
        if (d != null && t < d) return false;
        if (h != null && t > h) return false;
        return true;
      })
      .sort((a, b) => {
        const ta = a.fecha ? new Date(a.fecha).getTime() : 0;
        const tb = b.fecha ? new Date(b.fecha).getTime() : 0;
        return asc ? ta - tb : tb - ta;
      });
  });

  // Formulario de envío
  mostrarForm = signal(false);
  titulo = signal('');
  mensaje = signal('');
  categoria = signal<string | null>(null);
  enviando = signal(false);
  formMsg = signal('');

  // Segmentación del destinatario
  destinatario = signal<Destinatario>('EMPRESA');
  sucursalId = signal<number | null>(null);
  turnoId = signal<number | null>(null);
  rol = signal<string | null>(null);

  // Agente de IA (redactar con IA)
  instruccionIa = signal('');
  redactando = signal(false);
  iaMsg = signal('');
  asistidoConIa = signal(false);

  ngOnInit(): void {
    this.cargar();
    if (this.puedeEnviar) {
      this.cargarEnviadas();
      this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });
      this.turnoService.activos().subscribe({ next: (t) => this.turnos.set(t) });
    }
  }

  cargarEnviadas(): void {
    this.notificacionService.enviadas().subscribe({
      next: (e) => this.enviadas.set(e)
    });
  }

  eliminarEnviada(n: NotificacionEnviada, ev: Event): void {
    ev.stopPropagation();
    this.notificacionService.eliminarEnviada(n.id).subscribe({
      next: () => this.enviadas.update(list => list.filter(x => x.id !== n.id))
    });
  }

  /** Cambia el tipo de destinatario y limpia los objetivos que no aplican. */
  setDestinatario(valor: string): void {
    this.destinatario.set(valor as Destinatario);
    this.sucursalId.set(null);
    this.turnoId.set(null);
    this.rol.set(null);
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

  eliminar(n: Notificacion, ev: Event): void {
    ev.stopPropagation();
    this.notificacionService.eliminar(n.id).subscribe({
      next: () => this.notificaciones.update(list => list.filter(x => x.id !== n.id))
    });
  }

  /** La fecha llega en ISO; la mostramos en formato local dd/mm/aaaa hh:mm. */
  fechaLegible(iso: string | null): string {
    if (!iso) return '';
    const d = new Date(iso);
    return isNaN(d.getTime()) ? iso
      : d.toLocaleString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
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
    const dest = this.destinatario();
    if (dest === 'SUCURSAL' && this.sucursalId() == null) { this.formMsg.set('Elegí una sucursal.'); return; }
    if (dest === 'TURNO' && this.turnoId() == null) { this.formMsg.set('Elegí un turno.'); return; }
    if (dest === 'ROL' && !this.rol()) { this.formMsg.set('Elegí un rol.'); return; }

    const request: CrearNotificacionRequest = {
      titulo: this.titulo(), mensaje: this.mensaje(), categoria: this.categoria(),
      asistidoPorIa: this.asistidoConIa(),
      sucursalId: dest === 'SUCURSAL' ? this.sucursalId() : null,
      turnoId: dest === 'TURNO' ? this.turnoId() : null,
      rol: dest === 'ROL' ? this.rol() : null
    };

    this.formMsg.set('');
    this.enviando.set(true);
    this.notificacionService.enviar(request).subscribe({
      next: (r) => {
        this.enviando.set(false);
        this.formMsg.set(`Enviada a ${r.enviados} empleado(s).`);
        this.resetForm();
        this.cargarEnviadas();
        this.vista.set('enviadas');
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
    this.destinatario.set('EMPRESA');
    this.sucursalId.set(null); this.turnoId.set(null); this.rol.set(null);
    this.instruccionIa.set('');
    this.iaMsg.set(''); this.asistidoConIa.set(false);
  }

  icono(categoria: string | null): string {
    return categoria === 'PAGOS' ? '💸' : categoria === 'HORARIO' ? '🕒'
      : categoria === 'STOCK' ? '📦' : categoria === 'OPERATIVA' ? '⚙️' : '🔔';
  }
}
