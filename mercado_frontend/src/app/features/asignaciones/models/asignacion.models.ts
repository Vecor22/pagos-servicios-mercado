export interface AsignacionRequestDTO {
  codigoSocio: string;
  codigoPuesto: string;
}

export interface AsignacionResponseDTO {
  id: number;
  codigoSocio: string;
  dniSocio: string;
  nombreCompletoSocio: string;
  codigoPuesto: string;
  fechaAsignacion: string;
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
