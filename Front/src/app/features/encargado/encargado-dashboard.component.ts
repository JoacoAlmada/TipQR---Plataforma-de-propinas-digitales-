import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';
import { EmpleadoService } from '../../core/services/empleado.service';
import { MesaService } from '../../core/services/mesa.service';
import { GrupoPropinaService } from '../../core/services/grupo-propina.service';
import { TurnoService } from '../../core/services/turno.service';
import { Sucursal } from '../../core/models/sucursal.model';
import { Empleado } from '../../core/models/empleado.model';
import { Mesa } from '../../core/models/mesa.model';
import { GrupoPropina } from '../../core/models/grupo-propina.model';
import { Turno } from '../../core/models/turno.model';

@Component({
  selector: 'app-encargado-dashboard',
  standalone: true,
  imports: [],
  templateUrl: './encargado-dashboard.component.html'
})
export class EncargadoDashboardComponent implements OnInit {
  private readonly empleadoService = inject(EmpleadoService);
  private readonly mesaService = inject(MesaService);
  private readonly grupoService = inject(GrupoPropinaService);
  private readonly turnoService = inject(TurnoService);

  usuario = inject(AuthService).getUsuario();

  sucursal = signal<Sucursal | null>(null);
  empleados = signal<Empleado[]>([]);
  mesas = signal<Mesa[]>([]);
  grupos = signal<GrupoPropina[]>([]);
  turnoActivo = signal<Turno | null>(null);
  grupoElegido = signal<number | null>(null);
  nombreTurno = signal('');
  turnoMsg = signal('');
  loading = signal(true);
  errorMsg = signal('');

  ngOnInit(): void {
    this.empleadoService.miSucursal().subscribe({
      next: (suc) => {
        this.sucursal.set(suc);
        this.empleadoService.listar(suc.id).subscribe({ next: (e) => this.empleados.set(e) });
        this.mesaService.listar(suc.id).subscribe({ next: (m) => this.mesas.set(m) });
        this.grupoService.listar(suc.id).subscribe({
          next: (g) => { this.grupos.set(g); this.loading.set(false); },
          error: () => this.loading.set(false)
        });
        this.cargarTurno(suc.id);
      },
      error: () => {
        this.errorMsg.set('No tenés una sucursal asignada como encargado.');
        this.loading.set(false);
      }
    });
  }

  private cargarTurno(sucursalId: number): void {
    this.turnoService.activo(sucursalId).subscribe({
      next: (t) => this.turnoActivo.set(t)
    });
  }

  abrirTurno(): void {
    const suc = this.sucursal();
    const grupoId = this.grupoElegido();
    if (!suc || !grupoId) { this.turnoMsg.set('Elegí un grupo para abrir el turno.'); return; }
    this.turnoMsg.set('');
    this.turnoService.abrir({ sucursalId: suc.id, grupoId, nombre: this.nombreTurno() || null }).subscribe({
      next: (t) => { this.turnoActivo.set(t); this.grupoElegido.set(null); this.nombreTurno.set(''); },
      error: (err) => this.turnoMsg.set(err?.error?.error ?? 'No se pudo abrir el turno')
    });
  }

  cerrarTurno(): void {
    const suc = this.sucursal();
    if (!suc) return;
    this.turnoService.cerrar(suc.id).subscribe({
      next: () => this.turnoActivo.set(null),
      error: (err) => this.turnoMsg.set(err?.error?.error ?? 'No se pudo cerrar el turno')
    });
  }
}
