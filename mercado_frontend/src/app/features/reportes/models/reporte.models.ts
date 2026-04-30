export interface ResumenFlujoCajaResponseDTO {
  inicio: string;
  fin: string;
  totalIngresos: number;
  cantidadPagos: number;
}

export interface ResumenDeudasResponseDTO {
  inicio: string;
  fin: string;
  montoTotalPagables: number;
  montoTotalPagadas: number;
  montoTotalPendientes: number;
  porcentajePagadas: number;
  porcentajePendientes: number;
  cantidadTotalPagables: number;
  cantidadPagadas: number;
  cantidadPendientes: number;
  cantidadExoneradas: number;
  cantidadDistribuidas: number;
}

export interface ApiErrorResponse {
  timestamp?: string;
  status: number;
  error?: string;
  message?: string;
  path?: string;
  errors?: Record<string, string>;
}
