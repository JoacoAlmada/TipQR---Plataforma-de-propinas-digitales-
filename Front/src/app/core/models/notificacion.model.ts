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

/** Notificación tal como la ve quien la envió: a quién llegó y cuántos la leyeron. */
export interface NotificacionEnviada {
  id: number;
  titulo: string;
  mensaje: string;
  categoria: string | null;
  prioridad: string | null;
  origen: string | null;
  destino: string;
  destinatarios: number;
  leidas: number;
  fecha: string;
}

export interface CrearNotificacionRequest {
  titulo: string;
  mensaje: string;
  categoria?: string | null;
  prioridad?: string | null;
  turnoId?: number | null;
  rol?: string | null;
  sucursalId?: number | null;
  asistidoPorIa?: boolean;
}

/** Borrador que devuelve el agente de IA a partir de una instrucción informal. */
export interface RedaccionNotificacion {
  titulo: string;
  mensaje: string;
  categoria: string | null;
  generadoPorIa: boolean;
}
