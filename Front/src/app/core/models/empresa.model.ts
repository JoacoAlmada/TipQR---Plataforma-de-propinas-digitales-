export interface Empresa {
  id: number;
  nombre: string;
  nombreFantasia: string | null;
  rubro: string | null;
  cuit: string | null;
  provincia: string | null;
  calle: string | null;
  numeracion: string | null;
  emailContacto: string | null;
  telefono: string | null;
  estado: boolean;
  fechaCreacion: string;
  estadoValidacion?: 'APROBADA' | 'PENDIENTE' | 'RECHAZADA';
  motivoRechazo?: string | null;
  constanciaCargada?: boolean;
  activa?: boolean;
}

export interface EmpresaRequest {
  nombre: string;
  nombreFantasia?: string | null;
  rubro?: string | null;
  cuit?: string | null;
  provincia?: string | null;
  calle?: string | null;
  numeracion?: string | null;
  emailContacto?: string | null;
  telefono?: string | null;
}

export type TipoDocumento = 'DNI_FRENTE' | 'DNI_DORSO' | 'SELFIE' | 'CONSTANCIA_AFIP';

/** Estado de un documento del dueño (constancia AFIP, DNI, selfie). */
export interface MiDocumento {
  tipo: TipoDocumento;
  cargado: boolean;
  nombreArchivo: string | null;
  contentType: string | null;
  esPdf: boolean;
  fechaCarga: string | null;
}
