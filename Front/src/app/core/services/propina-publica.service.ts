import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { QrDestino, OrdenEstado, PagoIniciado, MesaDestinatarios } from '../models/propina.model';

/** Servicio de la pantalla pública del cliente (sin login). */
@Injectable({ providedIn: 'root' })
export class PropinaPublicaService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api';

  resolverQr(codigo: string): Observable<QrDestino> {
    return this.http.get<QrDestino>(`${this.API}/public/qr/${codigo}`);
  }

  /** Para un QR de mesa: mozos del turno activo (o turnoActivo=false si no hay turno abierto). */
  destinatariosMesa(codigo: string): Observable<MesaDestinatarios> {
    return this.http.get<MesaDestinatarios>(`${this.API}/public/qr/${codigo}/destinatarios`);
  }

  /** empleadoId solo aplica a QR de mesa (individual a un mozo); null = al equipo / según destino. */
  crearOrden(codigo: string, monto: number, empleadoId: number | null = null): Observable<OrdenEstado> {
    return this.http.post<OrdenEstado>(`${this.API}/public/qr/${codigo}/ordenes`, { monto, empleadoId });
  }

  /** Crea la preferencia de Mercado Pago y devuelve la URL del Checkout Pro. */
  iniciarPago(ordenCodigo: string): Observable<PagoIniciado> {
    return this.http.post<PagoIniciado>(`${this.API}/public/ordenes/${ordenCodigo}/pago`, null);
  }

  estadoOrden(ordenCodigo: string): Observable<OrdenEstado> {
    return this.http.get<OrdenEstado>(`${this.API}/ordenes/${ordenCodigo}/estado`);
  }
}
