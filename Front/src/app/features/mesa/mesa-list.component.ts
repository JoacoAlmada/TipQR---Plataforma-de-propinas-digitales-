import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MesaService } from '../../core/services/mesa.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { Mesa } from '../../core/models/mesa.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-mesa-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './mesa-list.component.html'
})
export class MesaListComponent implements OnInit {
  private readonly mesaService = inject(MesaService);
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  mesas = signal<Mesa[]>([]);
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
    this.mesaService.listar(this.filtroSucursal()).subscribe({
      next: (data) => { this.mesas.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar las mesas'); this.loading.set(false); }
    });
  }

  onFiltro(value: string): void {
    this.filtroSucursal.set(value ? Number(value) : null);
    this.cargar();
  }

  toggleEstado(m: Mesa): void {
    this.errorMsg.set('');
    this.mesaService.cambiarEstado(m.id, !m.estado).subscribe({
      next: (act) => this.mesas.update(list => list.map(x => x.id === act.id ? act : x)),
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar el estado')
    });
  }
}
