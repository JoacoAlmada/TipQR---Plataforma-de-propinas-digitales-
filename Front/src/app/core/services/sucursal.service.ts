import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Sucursal, SucursalRequest } from '../models/sucursal.model';

@Injectable({ providedIn: 'root' })
export class SucursalService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/sucursales';

  listar(): Observable<Sucursal[]> {
    return this.http.get<Sucursal[]>(this.API);
  }

  obtener(id: number): Observable<Sucursal> {
    return this.http.get<Sucursal>(`${this.API}/${id}`);
  }

  crear(request: SucursalRequest): Observable<Sucursal> {
    return this.http.post<Sucursal>(this.API, request);
  }

  actualizar(id: number, request: SucursalRequest): Observable<Sucursal> {
    return this.http.put<Sucursal>(`${this.API}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<Sucursal> {
    const params = new HttpParams().set('estado', estado);
    return this.http.patch<Sucursal>(`${this.API}/${id}/estado`, null, { params });
  }
}
