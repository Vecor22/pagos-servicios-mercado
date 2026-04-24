export type EstadoSocio = 'ACTIVO' | 'INACTIVO';

export interface SocioRequestDTO {
  nombres: string;
  apellidos: string;
  dni: string;
  telefono: string;
  correo: string;
  estado: EstadoSocio;
}

export interface SocioResponseDTO {
  id: number;
  codigoSocio: string;
  nombres: string;
  apellidos: string;
  dni: string;
  telefono: string;
  correo: string;
  estado: EstadoSocio;
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
