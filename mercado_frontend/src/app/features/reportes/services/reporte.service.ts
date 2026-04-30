import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ResumenDeudasResponseDTO, ResumenFlujoCajaResponseDTO } from '../models/reporte.models';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/reportes';

  flujoCajaPorDia(fecha: string): Observable<ResumenFlujoCajaResponseDTO> {
    return this.api.get<ResumenFlujoCajaResponseDTO>(`${this.basePath}/resumen-flujo-caja/dia`, { fecha });
  }

  flujoCajaPorMes(anio: number, mes: number): Observable<ResumenFlujoCajaResponseDTO> {
    return this.api.get<ResumenFlujoCajaResponseDTO>(`${this.basePath}/resumen-flujo-caja/mes`, { anio, mes });
  }

  flujoCajaPorAnio(anio: number): Observable<ResumenFlujoCajaResponseDTO> {
    return this.api.get<ResumenFlujoCajaResponseDTO>(`${this.basePath}/resumen-flujo-caja/anio`, { anio });
  }

  flujoCajaPorFechas(inicio: string, fin: string): Observable<ResumenFlujoCajaResponseDTO> {
    return this.api.get<ResumenFlujoCajaResponseDTO>(`${this.basePath}/resumen-flujo-caja/fechas`, { inicio, fin });
  }

  resumenDeudasPorFechas(inicio: string, fin: string): Observable<ResumenDeudasResponseDTO> {
    return this.api.get<ResumenDeudasResponseDTO>(`${this.basePath}/resumen-deudas/fechas`, { inicio, fin });
  }
}
