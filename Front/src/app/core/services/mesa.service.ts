import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Mesa, MesaRequest } from '../models/mesa.model';

@Injectable({ providedIn: 'root' })
export class MesaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/mesas';

  listar(sucursalId?: number | null): Observable<Mesa[]> {
    let params = new HttpParams();
    if (sucursalId != null) params = params.set('sucursalId', sucursalId);
    return this.http.get<Mesa[]>(this.API, { params });
  }

  obtener(id: number): Observable<Mesa> {
    return this.http.get<Mesa>(`${this.API}/${id}`);
  }

  crear(request: MesaRequest): Observable<Mesa> {
    return this.http.post<Mesa>(this.API, request);
  }

  actualizar(id: number, request: MesaRequest): Observable<Mesa> {
    return this.http.put<Mesa>(`${this.API}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<Mesa> {
    const params = new HttpParams().set('estado', estado);
    return this.http.patch<Mesa>(`${this.API}/${id}/estado`, null, { params });
  }
}
