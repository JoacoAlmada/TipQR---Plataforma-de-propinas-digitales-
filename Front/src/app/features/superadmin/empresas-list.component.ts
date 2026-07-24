import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SuperadminService, EmpresaValidacion } from '../../core/services/superadmin.service';

@Component({
  selector: 'app-empresas-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './empresas-list.component.html'
})
export class EmpresasListComponent implements OnInit {
  private readonly superadmin = inject(SuperadminService);

  empresas = signal<EmpresaValidacion[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  readonly filtros = [
    { valor: 'PENDIENTE', label: 'Pendientes' },
    { valor: 'APROBADA', label: 'Aprobadas' },
    { valor: 'RECHAZADA', label: 'Rechazadas' }
  ];
  estado = signal('PENDIENTE');

  ngOnInit(): void {
    this.cargar();
  }

  cambiarFiltro(valor: string): void {
    if (valor === this.estado()) return;
    this.estado.set(valor);
    this.cargar();
  }

  private cargar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.superadmin.empresas(this.estado()).subscribe({
      next: (data) => { this.empresas.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar las empresas'); this.loading.set(false); }
    });
  }
}
