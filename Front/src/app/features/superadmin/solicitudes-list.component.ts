import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SuperadminService, SolicitudResumen } from '../../core/services/superadmin.service';

@Component({
  selector: 'app-solicitudes-list',
  standalone: true,
  imports: [RouterLink, DatePipe],
  templateUrl: './solicitudes-list.component.html'
})
export class SolicitudesListComponent implements OnInit {
  private readonly superadmin = inject(SuperadminService);

  solicitudes = signal<SolicitudResumen[]>([]);
  loading = signal(true);
  errorMsg = signal('');

  readonly filtros = [
    { valor: 'PENDIENTE_VALIDACION', label: 'Pendientes' },
    { valor: 'APROBADA', label: 'Aprobadas' },
    { valor: 'RECHAZADA', label: 'Rechazadas' }
  ];
  estado = signal('PENDIENTE_VALIDACION');

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
    this.superadmin.solicitudes(this.estado()).subscribe({
      next: (data) => { this.solicitudes.set(data); this.loading.set(false); },
      error: () => { this.errorMsg.set('No se pudieron cargar las solicitudes'); this.loading.set(false); }
    });
  }
}
