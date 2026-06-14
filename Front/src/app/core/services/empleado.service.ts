import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empleado, EmpleadoRequest } from '../models/empleado.model';
import { Sucursal } from '../models/sucursal.model';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/empleados';

  listar(sucursalId?: number | null): Observable<Empleado[]> {
    let params = new HttpParams();
    if (sucursalId != null) params = params.set('sucursalId', sucursalId);
    return this.http.get<Empleado[]>(this.API, { params });
  }

  obtener(id: number): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.API}/${id}`);
  }

  crear(request: EmpleadoRequest): Observable<Empleado> {
    return this.http.post<Empleado>(this.API, request);
  }

  actualizar(id: number, request: EmpleadoRequest): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.API}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<Empleado> {
    const params = new HttpParams().set('estado', estado);
    return this.http.patch<Empleado>(`${this.API}/${id}/estado`, null, { params });
  }

  marcarEncargado(id: number, valor: boolean): Observable<Empleado> {
    const params = new HttpParams().set('valor', valor);
    return this.http.patch<Empleado>(`${this.API}/${id}/encargado`, null, { params });
  }

  /** Sucursal del usuario logueado (panel del encargado). */
  miSucursal(): Observable<Sucursal> {
    return this.http.get<Sucursal>(`${this.API}/mi-sucursal`);
  }
}
