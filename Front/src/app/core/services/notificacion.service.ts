import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notificacion, CrearNotificacionRequest } from '../models/notificacion.model';

@Injectable({ providedIn: 'root' })
export class NotificacionService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/notificaciones';

  /** Bandeja del usuario logueado. */
  mias(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.API}/mias`);
  }

  /** Cantidad de no leídas (para el badge). */
  noLeidas(): Observable<{ noLeidas: number }> {
    return this.http.get<{ noLeidas: number }>(`${this.API}/mias/no-leidas`);
  }

  marcarLeida(id: number): Observable<void> {
    return this.http.patch<void>(`${this.API}/mias/${id}/leida`, null);
  }

  /** Envío de un aviso (dueño/encargado). */
  enviar(request: CrearNotificacionRequest): Observable<{ enviados: number }> {
    return this.http.post<{ enviados: number }>(this.API, request);
  }
}
