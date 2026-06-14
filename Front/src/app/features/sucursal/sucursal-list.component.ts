import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SucursalService } from '../../core/services/sucursal.service';
import { AuthService } from '../../core/services/auth.service';
import { Sucursal } from '../../core/models/sucursal.model';

@Component({
  selector: 'app-sucursal-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './sucursal-list.component.html'
})
export class SucursalListComponent implements OnInit {
  private readonly sucursalService = inject(SucursalService);
  private readonly auth = inject(AuthService);

  sucursales = signal<Sucursal[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  readonly esDueno = this.auth.hasRole('DUENO');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.sucursalService.listar().subscribe({
      next: (data) => { this.sucursales.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar las sucursales'); this.loading.set(false); }
    });
  }

  toggleEstado(s: Sucursal): void {
    this.errorMsg.set('');
    this.sucursalService.cambiarEstado(s.id, !s.estado).subscribe({
      next: (actualizada) => {
        this.sucursales.update(list => list.map(x => x.id === actualizada.id ? actualizada : x));
      },
      error: (err) => this.errorMsg.set(err?.error?.error ?? 'No se pudo cambiar el estado')
    });
  }
}
