export interface Mesa {
  id: number;
  numero: number;
  descripcion: string | null;
  estado: boolean;
  sucursalId: number;
  sucursalNombre: string;
}

export interface MesaRequest {
  numero: number;
  descripcion?: string | null;
  sucursalId: number;
}
