import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { DeudaRequestDTO, DeudaResponseDTO, EstadoDeuda, PageResponseDTO } from '../models/deuda.models';

@Injectable({
  providedIn: 'root'
})
export class DeudaService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/deudas';

  crear(request: DeudaRequestDTO): Observable<DeudaResponseDTO> {
    return this.api.post<DeudaResponseDTO, DeudaRequestDTO>(this.basePath, request);
  }

  listar(
    page: number,
    size: number,
    filtros?: {
      codigoPuesto?: string;
      estado?: EstadoDeuda | '';
      inicio?: string;
      fin?: string;
    }
  ): Observable<PageResponseDTO<DeudaResponseDTO>> {
    return this.api.get<PageResponseDTO<DeudaResponseDTO>>(this.basePath, {
      page,
      size,
      codigoPuesto: filtros?.codigoPuesto,
      estado: filtros?.estado,
      inicio: filtros?.inicio,
      fin: filtros?.fin
    });
  }

  exonerar(id: number, motivo: string): Observable<DeudaResponseDTO> {
    return this.api.patch<DeudaResponseDTO, null>(
      `${this.basePath}/${id}/exonerar?motivo=${encodeURIComponent(motivo)}`,
      null
    );
  }
}
