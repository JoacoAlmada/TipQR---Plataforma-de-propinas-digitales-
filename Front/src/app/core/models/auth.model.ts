export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  rol: string;
  nombre: string;
  apellido: string;
}

export interface UsuarioLogueado {
  email: string;
  rol: string;
  nombre: string;
  apellido: string;
}
