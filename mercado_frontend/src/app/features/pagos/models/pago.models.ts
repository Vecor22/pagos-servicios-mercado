export type EstadoPago = 'REGISTRADO' | 'ANULADO';

export interface PagoRequestDTO {
  codigoDeuda: string;
  montoPagado: number;
  medioPago: string;
  numeroOperacion: string;
}

export interface PagoResponseDTO {
  id: number;
  codigoPago: string;
  idDeuda: number;
  codigoDeuda: string;
  codigoPuesto: string | null;
  nombreCompletoSocio: string | null;
  montoPagado: number;
  medioPago: string;
  numeroOperacion: string | null;
  fechaPago: string;
  estado: EstadoPago;
  registradoPorUsername: string | null;
  anuladoPorUsername: string | null;
  fechaAnulacion: string | null;
  motivoAnulacion: string | null;
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
