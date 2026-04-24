export type TipoCobro = 'FIJO' | 'CONSUMO';

export interface ConceptoRequestDTO {
  nombre: string;
  descripcion: string;
  tipoCobro: TipoCobro;
}

export interface ConceptoResponseDTO {
  id: number;
  nombre: string;
  descripcion: string;
  tipoCobro: TipoCobro;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}
