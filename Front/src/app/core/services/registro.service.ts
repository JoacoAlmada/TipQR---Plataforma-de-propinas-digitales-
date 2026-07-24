import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface RegistroEstado {
  registroToken: string;
  estadoCuenta: string;
  emailVerificado: boolean;
}

export interface RegistroPaso1 {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  telefono: string;
  cuit: string;
  dni: string;
  captchaToken: string;
}

export interface RegistroPaso2 {
  nombreEmpresa: string;
  nombreFantasia?: string | null;
  provincia: string;
  calle: string;
  numeracion: string;
  cuit?: string | null;
  rubro: string;
}

export type TipoDocumento = 'DNI_FRENTE' | 'DNI_DORSO' | 'SELFIE' | 'CONSTANCIA_AFIP';

export interface RegistroResumen {
  registroToken: string;
  estadoCuenta: string;
  motivoRechazo: string | null;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  cuit: string;
  dni: string;
  nombreEmpresa: string | null;
  nombreFantasia: string | null;
  provincia: string | null;
  calle: string | null;
  numeracion: string | null;
  empresaCuit: string | null;
  rubro: string | null;
  documentosCargados: TipoDocumento[];
}

export interface RegistroDatos {
  nombre: string;
  apellido: string;
  telefono: string;
  cuit: string;
  dni: string;
}

@Injectable({ providedIn: 'root' })
export class RegistroService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/registro';

  paso1(body: RegistroPaso1): Observable<RegistroEstado> {
    return this.http.post<RegistroEstado>(`${this.API}/paso1`, body);
  }

  estado(token: string): Observable<RegistroEstado> {
    const params = new HttpParams().set('token', token);
    return this.http.get<RegistroEstado>(`${this.API}/estado`, { params });
  }

  paso2(token: string, body: RegistroPaso2): Observable<void> {
    const params = new HttpParams().set('token', token);
    return this.http.post<void>(`${this.API}/paso2`, body, { params });
  }

  subirDocumento(token: string, tipo: TipoDocumento, archivo: File): Observable<void> {
    const params = new HttpParams().set('token', token).set('tipo', tipo);
    const fd = new FormData();
    fd.append('archivo', archivo);
    return this.http.post<void>(`${this.API}/documentos`, fd, { params });
  }

  documentos(token: string): Observable<TipoDocumento[]> {
    const params = new HttpParams().set('token', token);
    return this.http.get<TipoDocumento[]>(`${this.API}/documentos`, { params });
  }

  finalizar(token: string): Observable<void> {
    const params = new HttpParams().set('token', token);
    return this.http.post<void>(`${this.API}/finalizar`, null, { params });
  }

  /** Datos del registro para retomarlo tras un rechazo. */
  resumen(token: string): Observable<RegistroResumen> {
    const params = new HttpParams().set('token', token);
    return this.http.get<RegistroResumen>(`${this.API}/resumen`, { params });
  }

  /** Corrige los datos personales del dueño al retomar el registro. */
  actualizarDatos(token: string, body: RegistroDatos): Observable<void> {
    const params = new HttpParams().set('token', token);
    return this.http.put<void>(`${this.API}/datos`, body, { params });
  }
}
