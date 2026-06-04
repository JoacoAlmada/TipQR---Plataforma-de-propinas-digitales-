export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegistroRequest {
  nombreEmpresa: string;
  rubro?: string | null;
  cuit?: string | null;
  nombre: string;
  apellido: string;
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
