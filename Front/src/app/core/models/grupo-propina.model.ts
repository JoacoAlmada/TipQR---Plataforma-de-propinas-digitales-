export interface GrupoPropina {
  id: number;
  nombre: string;
  descripcion: string | null;
  tipoGrupo: string | null;
  estado: boolean;
  sucursalId: number;
  sucursalNombre: string;
}

export interface GrupoPropinaRequest {
  nombre: string;
  descripcion?: string | null;
  tipoGrupo?: string | null;
  sucursalId: number;
}

export interface MiembroGrupo {
  empleadoId: number;
  nombreVisible: string;
  apellido: string;
  email: string;
  puesto: string | null;
}
