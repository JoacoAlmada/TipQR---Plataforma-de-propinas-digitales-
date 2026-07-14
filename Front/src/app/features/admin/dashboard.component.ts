import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { EmpresaService } from '../../core/services/empresa.service';
import { DashboardService } from '../../core/services/dashboard.service';
import { Empresa } from '../../core/models/empresa.model';
import { ResumenDueno, RankingEmpleado, ReportePeriodo } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);
  private readonly dashboardService = inject(DashboardService);

  usuario = inject(AuthService).getUsuario();
  readonly esDueno = this.usuario?.rol === 'DUENO';

  empresa = signal<Empresa | null>(null);
  cargandoEmpresa = signal(true);

  resumen = signal<ResumenDueno | null>(null);
  cargandoResumen = signal(true);

  ranking = signal<RankingEmpleado[]>([]);
  reporte = signal<ReportePeriodo | null>(null);
  desde = signal(this.haceUnMes());
  hasta = signal(this.hoy());

  ngOnInit(): void {
    this.empresaService.miEmpresa().subscribe({
      next: (empresa) => {
        this.empresa.set(empresa);
        this.cargandoEmpresa.set(false);
      },
      error: () => this.cargandoEmpresa.set(false)
    });

    if (this.esDueno) {
      this.dashboardService.resumenDueno().subscribe({
        next: (r) => { this.resumen.set(r); this.cargandoResumen.set(false); },
        error: () => this.cargandoResumen.set(false)
      });
      this.dashboardService.ranking().subscribe({ next: (r) => this.ranking.set(r) });
      this.cargarReporte();
    } else {
      this.cargandoResumen.set(false);
    }
  }

  cargarReporte(): void {
    this.dashboardService.reporte(this.desde(), this.hasta()).subscribe({
      next: (r) => this.reporte.set(r)
    });
  }

  private hoy(): string {
    return new Date().toISOString().slice(0, 10);
  }
  private haceUnMes(): string {
    const d = new Date();
    d.setMonth(d.getMonth() - 1);
    return d.toISOString().slice(0, 10);
  }
}
