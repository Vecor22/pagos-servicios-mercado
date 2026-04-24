import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { PageResponseDTO, PagoRequestDTO, PagoResponseDTO } from '../models/pago.models';

@Injectable({
  providedIn: 'root'
})
export class PagoService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/pagos';

  listar(
    page: number,
    size: number,
    filtros?: {
      codigoPago?: string;
      codigoDeuda?: string;
      inicio?: string;
      fin?: string;
    }
  ): Observable<PageResponseDTO<PagoResponseDTO>> {
    return this.api.get<PageResponseDTO<PagoResponseDTO>>(this.basePath, {
      page,
      size,
      codigoPago: filtros?.codigoPago,
      codigoDeuda: filtros?.codigoDeuda,
      inicio: filtros?.inicio,
      fin: filtros?.fin
    });
  }

  registrar(request: PagoRequestDTO): Observable<PagoResponseDTO> {
    return this.api.post<PagoResponseDTO, PagoRequestDTO>(this.basePath, request);
  }

  obtenerPorId(id: number): Observable<PagoResponseDTO> {
    return this.api.get<PagoResponseDTO>(`${this.basePath}/${id}`);
  }

  obtenerPorDeuda(idDeuda: number): Observable<PagoResponseDTO> {
    return this.api.get<PagoResponseDTO>(`${this.basePath}/deuda/${idDeuda}`);
  }

  anular(id: number, motivo: string): Observable<PagoResponseDTO> {
    return this.api.patch<PagoResponseDTO, null>(
      `${this.basePath}/${id}/anular?motivo=${encodeURIComponent(motivo)}`,
      null
    );
  }
}
