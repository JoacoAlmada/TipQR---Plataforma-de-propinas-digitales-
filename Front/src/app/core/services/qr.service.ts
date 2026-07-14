import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Qr } from '../models/qr.model';

@Injectable({ providedIn: 'root' })
export class QrService {
  private readonly http = inject(HttpClient);
  private readonly API = 'http://localhost:8080/api/qr';

  listar(sucursalId?: number | null): Observable<Qr[]> {
    let params = new HttpParams();
    if (sucursalId != null) params = params.set('sucursalId', sucursalId);
    return this.http.get<Qr[]>(this.API, { params });
  }

  /** Imagen PNG del QR (con el JWT que agrega el interceptor). */
  imagenBlob(id: number): Observable<Blob> {
    return this.http.get(`${this.API}/${id}/imagen`, { responseType: 'blob' });
  }

  regenerar(id: number): Observable<Qr> {
    return this.http.post<Qr>(`${this.API}/${id}/regenerar`, null);
  }

  // ── Mi QR (empleado) ──
  miQr(): Observable<Qr> {
    return this.http.get<Qr>(`${this.API}/mi-qr`);
  }

  miQrImagen(): Observable<Blob> {
    return this.http.get(`${this.API}/mi-qr/imagen`, { responseType: 'blob' });
  }
}
