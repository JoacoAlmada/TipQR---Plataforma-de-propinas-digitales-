import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Turno, TurnoAbrirRequest } from '../models/turno.model';

@Injectable({ providedIn: 'root' })
export class TurnoService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/turnos';

  /** Turno activo de la sucursal (devuelve null si no hay ninguno abierto — el back responde 204). */
  activo(sucursalId: number): Observable<Turno | null> {
    const params = new HttpParams().set('sucursalId', sucursalId);
    return this.http.get<Turno | null>(`${this.API}/activo`, { params });
  }

  abrir(request: TurnoAbrirRequest): Observable<Turno> {
    return this.http.post<Turno>(`${this.API}/abrir`, request);
  }

  cerrar(sucursalId: number): Observable<Turno> {
    const params = new HttpParams().set('sucursalId', sucursalId);
    return this.http.post<Turno>(`${this.API}/cerrar`, null, { params });
  }
}
