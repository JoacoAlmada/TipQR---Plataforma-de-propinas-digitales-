import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegistroRequest, UsuarioLogueado } from '../models/auth.model';
import { StorageService } from './storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storage = inject(StorageService);

  private readonly API = 'http://localhost:8080/api/auth';
  private readonly TOKEN_KEY = 'tipqr_token';
  private readonly USER_KEY = 'tipqr_user';

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/login`, request).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  registrar(request: RegistroRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.API}/registro`, request).pipe(
      tap(response => this.guardarSesion(response))
    );
  }

  private guardarSesion(response: LoginResponse): void {
    this.storage.set(this.TOKEN_KEY, response.token);
    const user: UsuarioLogueado = {
      email: response.email,
      rol: response.rol,
      nombre: response.nombre,
      apellido: response.apellido
    };
    this.storage.set(this.USER_KEY, JSON.stringify(user));
  }

  logout(): void {
    this.storage.remove(this.TOKEN_KEY);
    this.storage.remove(this.USER_KEY);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.storage.get(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getUsuario(): UsuarioLogueado | null {
    const raw = this.storage.get(this.USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  hasRole(rol: string): boolean {
    return this.getUsuario()?.rol === rol;
  }
}
