import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empresa, EmpresaRequest } from '../models/empresa.model';

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/empresas';

  /** Empresa del usuario autenticado. */
  miEmpresa(): Observable<Empresa> {
    return this.http.get<Empresa>(`${this.API}/mia`);
  }

  actualizar(id: number, request: EmpresaRequest): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.API}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<Empresa> {
    const params = new HttpParams().set('estado', estado);
    return this.http.patch<Empresa>(`${this.API}/${id}/estado`, null, { params });
  }
}
