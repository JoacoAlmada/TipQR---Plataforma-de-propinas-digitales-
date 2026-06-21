export interface Qr {
  id: number;
  codigo: string;
  tipoDestino: 'MESA' | 'EMPLEADO' | 'GRUPO' | 'SUCURSAL';
  url: string;
  activo: boolean;
  destinoId: number | null;
  destinoNombre: string | null;
  sucursalId: number;
  sucursalNombre: string;
  imagenUrl: string;
}
