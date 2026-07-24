import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Empresa, EmpresaRequest, MiDocumento, TipoDocumento } from '../models/empresa.model';

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/empresas';
  private readonly DOCS = 'http://localhost:8080/api/perfil/documentos';

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

  // ── Multi-empresa (dueño con varias) ──

  /** Todas las empresas que administra el dueño (marca la activa). */
  misEmpresas(): Observable<Empresa[]> {
    return this.http.get<Empresa[]>(`${this.API}/mias`);
  }

  /** Da de alta una empresa adicional (queda pendiente de validación). */
  crear(request: EmpresaRequest): Observable<Empresa> {
    return this.http.post<Empresa>(this.API, request);
  }

  /** Sube/reemplaza la constancia de AFIP de una empresa pendiente o rechazada. */
  subirConstancia(id: number, archivo: File): Observable<Empresa> {
    const fd = new FormData();
    fd.append('archivo', archivo);
    return this.http.post<Empresa>(`${this.API}/${id}/constancia`, fd);
  }

  /** Reenvía a validación una empresa rechazada (tras corregirla). */
  reenviar(id: number): Observable<Empresa> {
    return this.http.post<Empresa>(`${this.API}/${id}/reenviar`, null);
  }

  /** Cambia la empresa que el dueño está gestionando. */
  activar(id: number): Observable<Empresa> {
    return this.http.put<Empresa>(`${this.API}/${id}/activar`, null);
  }

  // ── Documentos del dueño (registro) ──

  /** Estado de los documentos (constancia AFIP, DNI, selfie). */
  documentos(): Observable<MiDocumento[]> {
    return this.http.get<MiDocumento[]>(this.DOCS);
  }

  /** Binario de un documento (para previsualizar) — llega como Blob. */
  documentoBlob(tipo: TipoDocumento): Observable<Blob> {
    return this.http.get(`${this.DOCS}/${tipo}/archivo`, { responseType: 'blob' });
  }

  /** Sube o reemplaza un documento. */
  subirDocumento(tipo: TipoDocumento, archivo: File): Observable<MiDocumento> {
    const params = new HttpParams().set('tipo', tipo);
    const fd = new FormData();
    fd.append('archivo', archivo);
    return this.http.post<MiDocumento>(this.DOCS, fd, { params });
  }
}
