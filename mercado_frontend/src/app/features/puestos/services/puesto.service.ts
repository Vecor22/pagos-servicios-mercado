import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { PageResponseDTO, PuestoRequestDTO, PuestoResponseDTO } from '../models/puesto.models';

@Injectable({
  providedIn: 'root'
})
export class PuestoService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/puestos';

  listar(page: number, size: number, sinAsignacion = false): Observable<PageResponseDTO<PuestoResponseDTO>> {
    return this.api.get<PageResponseDTO<PuestoResponseDTO>>(this.basePath, { page, size, sinAsignacion });
  }

  crear(): Observable<PuestoResponseDTO> {
    const request: PuestoRequestDTO = {};
    return this.api.post<PuestoResponseDTO, PuestoRequestDTO>(this.basePath, request);
  }
}
