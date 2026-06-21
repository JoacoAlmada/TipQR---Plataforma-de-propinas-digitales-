export interface QrDestino {
  codigo: string;
  tipoDestino: 'MESA' | 'EMPLEADO' | 'GRUPO' | 'SUCURSAL';
  destinoNombre: string | null;
  sucursalNombre: string | null;
  empresaNombre: string | null;
  activo: boolean;
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
