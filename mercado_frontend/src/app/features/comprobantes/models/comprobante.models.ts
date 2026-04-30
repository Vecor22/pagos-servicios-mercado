export type EstadoComprobante = 'EMITIDO' | 'ANULADO';

export interface ComprobanteResponseDTO {
  id: number;
  idPago: number;
  codigoPuesto: string | null;
  dniSocio: string | null;
  nombreCompletoSocio: string | null;
  numeroComprobante: string;
  tipoComprobante: string;
  fechaEmision: string;
  estado: EstadoComprobante;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}
