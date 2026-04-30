export interface PuestoRequestDTO {
}

export interface PuestoResponseDTO {
  id: number;
  codigoPuesto: string;
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
