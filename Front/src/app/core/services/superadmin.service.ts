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

@Injectable({ providedIn: 'root' })
export class SuperadminService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/superadmin';

  solicitudes(): Observable<SolicitudResumen[]> {
    return this.http.get<SolicitudResumen[]>(`${this.API}/solicitudes`);
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
}
