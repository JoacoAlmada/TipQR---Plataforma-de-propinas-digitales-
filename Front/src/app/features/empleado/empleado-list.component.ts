import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { EmpleadoService } from '../../core/services/empleado.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { Empleado } from '../../core/models/empleado.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './empleado-list.component.html'
})
export class EmpleadoListComponent implements OnInit {
  private readonly empleadoService = inject(EmpleadoService);
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  empleados = signal<Empleado[]>([]);
  sucursales = signal<Sucursal[]>([]);
  filtroSucursal = signal<number | null>(null);
  loading = signal(true);
  errorMsg = signal('');

  readonly esDueno = this.auth.hasRole('DUENO');

  ngOnInit(): void {
    this.sucursalService.listar().subscribe({ next: (s) => this.sucursales.set(s) });
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.empleadoService.listar(this.filtroSucursal()).subscribe({
      next: (data) => { this.empleados.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar los empleados'); this.loading.set(false); }
    });
  }

  onFiltro(value: string): void {
    this.filtroSucursal.set(value ? Number(value) : null);
    this.cargar();
  }

  toggleEstado(e: Empleado): void {
    this.errorMsg.set('');
    this.empleadoService.cambiarEstado(e.id, !e.estado).subscribe({
      next: (act) => this.empleados.update(list => list.map(x => x.id === act.id ? act : x)),
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar el estado')
    });
  }

  toggleEncargado(e: Empleado): void {
    this.errorMsg.set('');
    this.empleadoService.marcarEncargado(e.id, !e.esEncargado).subscribe({
      next: (act) => this.empleados.update(list => list.map(x => x.id === act.id ? act : x)),
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar el rol')
    });
  }
}
