import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AsignacionRequestDTO, AsignacionResponseDTO, PageResponseDTO } from '../models/asignacion.models';

@Injectable({
  providedIn: 'root'
})
export class AsignacionService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/asignaciones';

  asignar(request: AsignacionRequestDTO): Observable<AsignacionResponseDTO> {
    return this.api.post<AsignacionResponseDTO, AsignacionRequestDTO>(this.basePath, request);
  }

  listar(
    page: number,
    size: number,
    filtros?: { dniSocio?: string; nombreSocio?: string }
  ): Observable<PageResponseDTO<AsignacionResponseDTO>> {
    return this.api.get<PageResponseDTO<AsignacionResponseDTO>>(this.basePath, {
      page,
      size,
      dniSocio: filtros?.dniSocio,
      nombreSocio: filtros?.nombreSocio
    });
  }

  reasignar(codigoPuesto: string, codigoSocio: string): Observable<AsignacionResponseDTO> {
    return this.api.patch<AsignacionResponseDTO, null>(
      `${this.basePath}/puesto/${encodeURIComponent(codigoPuesto)}/reasignar?codigoSocio=${encodeURIComponent(codigoSocio)}`,
      null
    );
  }
}
