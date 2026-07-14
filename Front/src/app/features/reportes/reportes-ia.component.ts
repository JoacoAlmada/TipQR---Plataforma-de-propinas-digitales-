import { Component, OnInit, inject, signal } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { ReporteAutomatico } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-reportes-ia',
  standalone: true,
  imports: [],
  templateUrl: './reportes-ia.component.html'
})
export class ReportesIaComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  reportes = signal<ReporteAutomatico[]>([]);
  loading = signal(true);
  generando = signal(false);
  msg = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.dashboardService.reportesIa().subscribe({
      next: (r) => { this.reportes.set(r); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  generar(): void {
    this.msg.set('');
    this.generando.set(true);
    this.dashboardService.generarReporteIa().subscribe({
      next: (r) => {
        this.generando.set(false);
        this.reportes.update(list => [r, ...list]);
        this.msg.set(r.generadoPorIa
          ? 'Resumen generado con IA.'
          : 'Resumen generado (redacción local — configurá GEMINI_API_KEY para usar IA).');
      },
      error: (err) => {
        this.generando.set(false);
        this.msg.set(err?.error?.error ?? 'No se pudo generar el reporte.');
      }
    });
  }
}
