import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { GrupoPropinaService } from '../../core/services/grupo-propina.service';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { GrupoPropina } from '../../core/models/grupo-propina.model';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-grupo-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './grupo-list.component.html'
})
export class GrupoListComponent implements OnInit {
  private readonly grupoService = inject(GrupoPropinaService);
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  grupos = signal<GrupoPropina[]>([]);
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
    this.grupoService.listar(this.filtroSucursal()).subscribe({
      next: (data) => { this.grupos.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar los grupos'); this.loading.set(false); }
    });
  }

  onFiltro(value: string): void {
    this.filtroSucursal.set(value ? Number(value) : null);
    this.cargar();
  }

  toggleEstado(g: GrupoPropina): void {
    this.errorMsg.set('');
    this.grupoService.cambiarEstado(g.id, !g.estado).subscribe({
      next: (act) => this.grupos.update(list => list.map(x => x.id === act.id ? act : x)),
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar el estado')
    });
  }
}
