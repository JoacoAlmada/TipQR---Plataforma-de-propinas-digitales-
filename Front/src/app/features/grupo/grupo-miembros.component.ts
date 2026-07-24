import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { GrupoPropinaService } from '../../core/services/grupo-propina.service';
import { EmpleadoService } from '../../core/services/empleado.service';
import { AuthService } from '../../core/services/auth.service';
import { GrupoPropina, MiembroGrupo, ItemPorcentaje } from '../../core/models/grupo-propina.model';
import { Empleado } from '../../core/models/empleado.model';

@Component({
  selector: 'app-grupo-miembros',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './grupo-miembros.component.html'
})
export class GrupoMiembrosComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly grupoService = inject(GrupoPropinaService);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly auth = inject(AuthService);

  private grupoId!: number;

  grupo = signal<GrupoPropina | null>(null);
  miembros = signal<MiembroGrupo[]>([]);
  empleadosSucursal = signal<Empleado[]>([]);
  loading = signal(true);
  errorMsg = signal('');
  procesando = signal<number | null>(null);

  // Distribución por porcentajes
  editandoPct = signal(false);
  pctInputs = signal<Record<number, number>>({});
  distribGuardando = signal(false);
  distribMsg = signal('');
  sumaPct = computed(() =>
    Object.values(this.pctInputs()).reduce((a, b) => a + (Number(b) || 0), 0));

  readonly esDueno = this.auth.hasRole('DUENO');

  /** Empleados de la sucursal que aún no están en el grupo. */
  disponibles = computed(() => {
    const ids = new Set(this.miembros().map(m => m.empleadoId));
    return this.empleadosSucursal().filter(e => !ids.has(e.id));
  });

  ngOnInit(): void {
    this.grupoId = Number(this.route.snapshot.paramMap.get('id'));
    this.grupoService.obtener(this.grupoId).subscribe({
      next: (g) => {
        this.grupo.set(g);
        this.empleadoService.listar(g.sucursalId).subscribe({ next: (e) => this.empleadosSucursal.set(e) });
        this.cargarMiembros();
      },
      error: () => { this.errorMsg.set('No se pudo cargar el grupo'); this.loading.set(false); }
    });
  }

  cargarMiembros(): void {
    this.grupoService.listarMiembros(this.grupoId).subscribe({
      next: (m) => { this.miembros.set(m); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar los miembros'); this.loading.set(false); }
    });
  }

  agregar(e: Empleado): void {
    this.procesando.set(e.id);
    this.errorMsg.set('');
    this.grupoService.agregarMiembro(this.grupoId, e.id).subscribe({
      next: () => { this.procesando.set(null); this.cargarMiembros(); },
      error: (err) => { this.procesando.set(null); this.errorMsg.set(err?.error?.error ?? 'No se pudo agregar'); }
    });
  }

  remover(m: MiembroGrupo): void {
    this.procesando.set(m.empleadoId);
    this.errorMsg.set('');
    this.grupoService.removerMiembro(this.grupoId, m.empleadoId).subscribe({
      next: () => { this.procesando.set(null); this.cargarMiembros(); },
      error: (err) => { this.procesando.set(null); this.errorMsg.set(err?.error?.error ?? 'No se pudo remover'); }
    });
  }

  // ── Distribución por porcentajes ──

  /** Abre el editor de porcentajes, precargando los actuales o un reparto equitativo. */
  abrirPorcentajes(): void {
    this.distribMsg.set('');
    const ms = this.miembros();
    const equit = ms.length ? Math.round((100 / ms.length) * 100) / 100 : 0;
    const inputs: Record<number, number> = {};
    ms.forEach(m => inputs[m.empleadoId] = m.porcentajeDistribucion ?? equit);
    this.pctInputs.set(inputs);
    this.editandoPct.set(true);
  }

  setPct(empleadoId: number, valor: string): void {
    this.pctInputs.update(prev => ({ ...prev, [empleadoId]: Number(valor) }));
  }

  guardarPorcentajes(): void {
    const porcentajes: ItemPorcentaje[] = this.miembros().map(m => ({
      empleadoId: m.empleadoId,
      porcentaje: Number(this.pctInputs()[m.empleadoId]) || 0
    }));
    this.distribMsg.set('');
    this.distribGuardando.set(true);
    this.grupoService.configurarPorcentajes(this.grupoId, porcentajes).subscribe({
      next: (g) => {
        this.distribGuardando.set(false);
        this.grupo.set(g);
        this.editandoPct.set(false);
        this.cargarMiembros();
        this.distribMsg.set('Porcentajes guardados. El grupo reparte por porcentaje.');
      },
      error: (err) => {
        this.distribGuardando.set(false);
        this.distribMsg.set(err?.error?.error ?? 'No se pudieron guardar los porcentajes.');
      }
    });
  }

  volverEquitativo(): void {
    this.distribMsg.set('');
    this.distribGuardando.set(true);
    this.grupoService.usarEquitativo(this.grupoId).subscribe({
      next: (g) => {
        this.distribGuardando.set(false);
        this.grupo.set(g);
        this.editandoPct.set(false);
        this.distribMsg.set('El grupo reparte en partes iguales.');
      },
      error: () => {
        this.distribGuardando.set(false);
        this.distribMsg.set('No se pudo cambiar el modo.');
      }
    });
  }
}
