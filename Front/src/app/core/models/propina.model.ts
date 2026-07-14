export interface QrDestino {
  codigo: string;
  tipoDestino: 'MESA' | 'EMPLEADO' | 'GRUPO' | 'SUCURSAL';
  destinoNombre: string | null;
  sucursalNombre: string | null;
  empresaNombre: string | null;
  activo: boolean;
}

export interface MozoTurno {
  empleadoId: number;
  nombre: string;
}

export interface MesaDestinatarios {
  codigo: string;
  destinoNombre: string | null;
  sucursalNombre: string | null;
  empresaNombre: string | null;
  turnoActivo: boolean;
  grupoNombre: string | null;
  mozos: MozoTurno[];
}

export interface PagoIniciado {
  ordenCodigo: string;
  preferenceId: string;
  checkoutUrl: string;
  publicKey: string;
}

export interface OrdenEstado {
  codigo: string;
  estado: string;
  tipoPropina: string;
  monto: number;
  fechaCreacion: string;
  fechaExpiracion: string | null;
  fechaPago: string | null;
  sucursalNombre: string | null;
}
