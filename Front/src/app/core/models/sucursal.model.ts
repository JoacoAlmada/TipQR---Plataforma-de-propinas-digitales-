export interface Sucursal {
  id: number;
  nombre: string;
  direccion: string | null;
  telefono: string | null;
  estado: boolean;
  fechaCreacion: string;
}

export interface SucursalRequest {
  nombre: string;
  direccion?: string | null;
  telefono?: string | null;
}
