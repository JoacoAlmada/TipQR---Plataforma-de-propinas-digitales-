export interface Empleado {
  id: number;
  nombreVisible: string;
  apellido: string;
  email: string;
  puesto: string | null;
  estado: boolean;
  esEncargado: boolean;
  sucursalId: number;
  sucursalNombre: string;
  fechaAlta: string;
  passwordTemporal?: string | null;
}

export interface EmpleadoRequest {
  nombreVisible: string;
  apellido: string;
  email: string;
  puesto?: string | null;
  sucursalId: number;
}
