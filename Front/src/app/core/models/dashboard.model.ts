export interface PropinaEmpleado {
  codigo: string;
  monto: number;
  mesa: string | null;
  fechaPago: string | null;
  tipo: 'INDIVIDUAL' | 'GRUPAL';
}

export interface HistorialPropinas {
  cantidad: number;
  total: number;
  propinas: PropinaEmpleado[];
}

export interface ResumenSucursal {
  sucursalId: number;
  sucursalNombre: string;
  total: number;
  cantidad: number;
}

export interface ResumenDueno {
  totalRecaudado: number;
  cantidadPropinas: number;
  ticketPromedio: number;
  porSucursal: ResumenSucursal[];
}

export interface RankingEmpleado {
  empleadoId: number;
  nombre: string;
  sucursal: string | null;
  cantidad: number;
  total: number;
}

export interface ReportePeriodo {
  desde: string;
  hasta: string;
  totalRecaudado: number;
  cantidadPropinas: number;
  ticketPromedio: number;
}

/** Reporte generado por el agente de IA. */
export interface ReporteAutomatico {
  id: number;
  titulo: string;
  contenido: string;
  totalRecaudado: number | null;
  cantidadPropinas: number | null;
  ticketPromedio: number | null;
  generadoPorIa: boolean;
  fecha: string | null;
  fechaIso: string | null;
}
