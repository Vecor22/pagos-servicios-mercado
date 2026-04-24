export interface ResumenFlujoCajaResponseDTO {
  inicio: string;
  fin: string;
  totalIngresos: number;
  cantidadPagos: number;
}

export interface ResumenDeudasResponseDTO {
  inicio: string;
  fin: string;
  totalDeuda: number;
  totalPagado: number;
  totalPendiente: number;
  totalExonerado: number;
  totalActualCaja: number;
  porcentajePagado: number;
  porcentajePendiente: number;
  porcentajeExonerado: number;
  cantidadDeudas: number;
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
