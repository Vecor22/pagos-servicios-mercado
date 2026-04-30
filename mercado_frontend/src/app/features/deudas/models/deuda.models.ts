export type EstadoDeuda = 'PENDIENTE' | 'PAGADA' | 'EXONERADA' | 'DISTRIBUIDA';
export type TipoGeneracionDeuda = 'INDIVIDUAL' | 'REPARTIBLE';

export interface DeudaRequestDTO {
  nombreConceptoCobro: string;
  codigoPuesto: string | null;
  monto: number;
  tipoGeneracion: TipoGeneracionDeuda;
  observacion: string;
}

export interface DeudaResponseDTO {
  id: number;
  codigoDeuda: string;
  idConceptoCobro: number;
  nombreConcepto: string;
  idPuesto: number | null;
  codigoPuesto: string | null;
  idSocio: number | null;
  nombreCompletoSocio: string | null;
  idDeudaOrigen: number | null;
  codigoDeudaOrigen: string | null;
  monto: number;
  tipoGeneracion: TipoGeneracionDeuda;
  estado: EstadoDeuda;
  fechaGeneracion: string;
  observacion: string | null;
  creadoPorUsername: string | null;
  actualizadoPorUsername: string | null;
  fechaActualizacion: string | null;
  exoneradoPorUsername: string | null;
  fechaExoneracion: string | null;
  motivoExoneracion: string | null;
}

export interface PageResponseDTO<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}
