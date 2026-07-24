import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GrupoPropina, GrupoPropinaRequest, MiembroGrupo, ItemPorcentaje } from '../models/grupo-propina.model';

@Injectable({ providedIn: 'root' })
export class GrupoPropinaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/grupos-propina';

  listar(sucursalId?: number | null): Observable<GrupoPropina[]> {
    let params = new HttpParams();
    if (sucursalId != null) params = params.set('sucursalId', sucursalId);
    return this.http.get<GrupoPropina[]>(this.API, { params });
  }

  obtener(id: number): Observable<GrupoPropina> {
    return this.http.get<GrupoPropina>(`${this.API}/${id}`);
  }

  crear(request: GrupoPropinaRequest): Observable<GrupoPropina> {
    return this.http.post<GrupoPropina>(this.API, request);
  }

  actualizar(id: number, request: GrupoPropinaRequest): Observable<GrupoPropina> {
    return this.http.put<GrupoPropina>(`${this.API}/${id}`, request);
  }

  cambiarEstado(id: number, estado: boolean): Observable<GrupoPropina> {
    const params = new HttpParams().set('estado', estado);
    return this.http.patch<GrupoPropina>(`${this.API}/${id}/estado`, null, { params });
  }

  // ── Miembros ──
  listarMiembros(grupoId: number): Observable<MiembroGrupo[]> {
    return this.http.get<MiembroGrupo[]>(`${this.API}/${grupoId}/empleados`);
  }

  agregarMiembro(grupoId: number, empleadoId: number): Observable<void> {
    return this.http.post<void>(`${this.API}/${grupoId}/empleados`, { empleadoId });
  }

  removerMiembro(grupoId: number, empleadoId: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/${grupoId}/empleados/${empleadoId}`);
  }

  // ── Distribución ──
  configurarPorcentajes(grupoId: number, porcentajes: ItemPorcentaje[]): Observable<GrupoPropina> {
    return this.http.put<GrupoPropina>(`${this.API}/${grupoId}/porcentajes`, { porcentajes });
  }

  usarEquitativo(grupoId: number): Observable<GrupoPropina> {
    return this.http.put<GrupoPropina>(`${this.API}/${grupoId}/equitativo`, null);
  }
}

