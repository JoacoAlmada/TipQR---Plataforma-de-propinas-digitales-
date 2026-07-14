import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistorialPropinas, ResumenDueno, RankingEmpleado, ReportePeriodo } from '../models/dashboard.model';
import { HttpParams } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api';

  /** Historial de propinas del empleado logueado. */
  historialEmpleado(): Observable<HistorialPropinas> {
    return this.http.get<HistorialPropinas>(`${this.API}/empleado/propinas`);
  }

  /** Resumen de propinas de la empresa (dueño). */
  resumenDueno(): Observable<ResumenDueno> {
    return this.http.get<ResumenDueno>(`${this.API}/dashboard/resumen`);
  }

  /** Ranking de empleados por propinas recibidas. */
  ranking(): Observable<RankingEmpleado[]> {
    return this.http.get<RankingEmpleado[]>(`${this.API}/dashboard/ranking`);
  }

  /** Reporte de propinas pagadas en un rango (yyyy-MM-dd). */
  reporte(desde: string, hasta: string): Observable<ReportePeriodo> {
    const params = new HttpParams().set('desde', desde).set('hasta', hasta);
    return this.http.get<ReportePeriodo>(`${this.API}/dashboard/reporte`, { params });
  }
}
