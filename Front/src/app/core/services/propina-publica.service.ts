import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QrDestino, OrdenEstado } from '../models/propina.model';

/** Servicio de la pantalla pública del cliente (sin login). */
@Injectable({ providedIn: 'root' })
export class PropinaPublicaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api';

  resolverQr(codigo: string): Observable<QrDestino> {
    return this.http.get<QrDestino>(`${this.API}/public/qr/${codigo}`);
  }

  crearOrden(codigo: string, monto: number): Observable<OrdenEstado> {
    return this.http.post<OrdenEstado>(`${this.API}/public/qr/${codigo}/ordenes`, { monto });
  }

  estadoOrden(ordenCodigo: string): Observable<OrdenEstado> {
    return this.http.get<OrdenEstado>(`${this.API}/ordenes/${ordenCodigo}/estado`);
  }
}
