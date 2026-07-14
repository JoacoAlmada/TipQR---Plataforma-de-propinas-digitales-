export interface Notificacion {
  id: number;
  titulo: string;
  mensaje: string;
  categoria: string | null;
  prioridad: string | null;
  origen: string | null;
  emisor: string;
  leida: boolean;
  fecha: string;
}

export interface CrearNotificacionRequest {
  titulo: string;
  mensaje: string;
  categoria?: string | null;
  prioridad?: string | null;
  sucursalId?: number | null;
}
