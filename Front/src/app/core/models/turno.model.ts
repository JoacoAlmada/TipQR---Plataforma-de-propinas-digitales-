export interface Turno {
  id: number;
  sucursalId: number;
  sucursalNombre: string;
  grupoId: number;
  grupoNombre: string;
  nombre: string | null;
  activo: boolean;
  abiertoPor: string | null;
  fechaApertura: string;
  fechaCierre: string | null;
}

export interface TurnoAbrirRequest {
  sucursalId: number;
  grupoId: number;
  nombre?: string | null;
}
