export interface Empresa {
  id: number;
  nombre: string;
  rubro: string | null;
  cuit: string | null;
  emailContacto: string | null;
  telefono: string | null;
  estado: boolean;
  fechaCreacion: string;
}

export interface EmpresaRequest {
  nombre: string;
  rubro?: string | null;
  cuit?: string | null;
  emailContacto?: string | null;
  telefono?: string | null;
}
