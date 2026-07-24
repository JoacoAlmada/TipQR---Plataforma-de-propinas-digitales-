import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SolicitudResumen {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  empresaNombre: string | null;
  fechaSolicitud: string;
}

export interface DocumentoMeta {
  id: number;
  tipo: string;
  nombreArchivo: string;
  contentType: string;
}

export interface SolicitudDetalle {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  cuit: string;
  dni: string;
  estadoCuenta: string;
  fechaSolicitud: string;
  empresaNombre: string;
  nombreFantasia: string | null;
  rubro: string;
  empresaCuit: string | null;
  provincia: string;
  calle: string;
  numeracion: string;
  documentos: DocumentoMeta[];
}

export interface EmpresaValidacion {
  id: number;
  nombre: string;
  nombreFantasia: string | null;
  rubro: string | null;
  cuit: string | null;
  provincia: string | null;
  calle: string | null;
  numeracion: string | null;
  emailContacto: string | null;
  telefono: string | null;
  estadoValidacion: string;
  motivoRechazo: string | null;
  fechaCreacion: string;
  constanciaCargada: boolean;
  constanciaNombre: string | null;
  constanciaContentType: string | null;
  propietarioNombre: string | null;
  propietarioApellido: string | null;
  propietarioEmail: string | null;
  propietarioTelefono: string | null;
}

@Injectable({ providedIn: 'root' })
export class SuperadminService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/superadmin';

  /** Cuentas por estado (PENDIENTE_VALIDACION por defecto). */
  solicitudes(estado?: string): Observable<SolicitudResumen[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<SolicitudResumen[]>(`${this.API}/solicitudes`, { params });
  }

  detalle(id: number): Observable<SolicitudDetalle> {
    return this.http.get<SolicitudDetalle>(`${this.API}/solicitudes/${id}`);
  }

  /** Descarga el binario del documento (con el JWT que agrega el interceptor). */
  documentoBlob(docId: number): Observable<Blob> {
    return this.http.get(`${this.API}/documentos/${docId}`, { responseType: 'blob' });
  }

  aprobar(id: number): Observable<void> {
    return this.http.post<void>(`${this.API}/solicitudes/${id}/aprobar`, null);
  }

  rechazar(id: number, motivo: string): Observable<void> {
    const params = new HttpParams().set('motivo', motivo);
    return this.http.post<void>(`${this.API}/solicitudes/${id}/rechazar`, null, { params });
  }

  // ── Validación de empresas nuevas ──

  /** Empresas por estado de validación (PENDIENTE por defecto). */
  empresas(estado?: string): Observable<EmpresaValidacion[]> {
    const params = estado ? new HttpParams().set('estado', estado) : undefined;
    return this.http.get<EmpresaValidacion[]>(`${this.API}/empresas`, { params });
  }

  empresa(id: number): Observable<EmpresaValidacion> {
    return this.http.get<EmpresaValidacion>(`${this.API}/empresas/${id}`);
  }

  /** Binario de la constancia de AFIP de la empresa. */
  constanciaBlob(id: number): Observable<Blob> {
    return this.http.get(`${this.API}/empresas/${id}/constancia`, { responseType: 'blob' });
  }

  aprobarEmpresa(id: number): Observable<void> {
    return this.http.post<void>(`${this.API}/empresas/${id}/aprobar`, null);
  }

  rechazarEmpresa(id: number, motivo: string): Observable<void> {
    const params = new HttpParams().set('motivo', motivo);
    return this.http.post<void>(`${this.API}/empresas/${id}/rechazar`, null, { params });
  }
}
