import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notificacion, NotificacionEnviada, CrearNotificacionRequest, RedaccionNotificacion } from '../models/notificacion.model';

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

  /** Elimina la notificación de la bandeja del usuario. */
  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/mias/${id}`);
  }

  /** Notificaciones que el usuario envió (dueño/encargado), con estadística de lectura. */
  enviadas(): Observable<NotificacionEnviada[]> {
    return this.http.get<NotificacionEnviada[]>(`${this.API}/enviadas`);
  }

  /** Elimina una notificación enviada (la borra para todos los destinatarios). */
  eliminarEnviada(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API}/enviadas/${id}`);
  }

  /** Envío de un aviso (dueño/encargado). */
  enviar(request: CrearNotificacionRequest): Observable<{ enviados: number }> {
    return this.http.post<{ enviados: number }>(this.API, request);
  }

  /** Pide al agente de IA que redacte un borrador a partir de una instrucción informal. */
  redactarIa(instruccion: string): Observable<RedaccionNotificacion> {
    return this.http.post<RedaccionNotificacion>(`${this.API}/redactar-ia`, { instruccion });
  }
}
