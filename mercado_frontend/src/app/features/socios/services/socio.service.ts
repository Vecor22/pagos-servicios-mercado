import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { EstadoSocio, PageResponseDTO, SocioRequestDTO, SocioResponseDTO } from '../models/socio.models';

@Injectable({
  providedIn: 'root'
})
export class SocioService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/socios';

  listar(page: number, size: number): Observable<PageResponseDTO<SocioResponseDTO>> {
    return this.api.get<PageResponseDTO<SocioResponseDTO>>(this.basePath, { page, size });
  }

  buscar(termino: string, page: number, size: number): Observable<PageResponseDTO<SocioResponseDTO>> {
    return this.api.get<PageResponseDTO<SocioResponseDTO>>(`${this.basePath}/buscar`, { termino, page, size });
  }

  obtenerPorId(id: number): Observable<SocioResponseDTO> {
    return this.api.get<SocioResponseDTO>(`${this.basePath}/${id}`);
  }

  crear(request: SocioRequestDTO): Observable<SocioResponseDTO> {
    return this.api.post<SocioResponseDTO, SocioRequestDTO>(this.basePath, request);
  }

  actualizar(id: number, request: SocioRequestDTO): Observable<SocioResponseDTO> {
    return this.api.put<SocioResponseDTO, SocioRequestDTO>(`${this.basePath}/${id}`, request);
  }

  cambiarEstado(id: number, estado: EstadoSocio): Observable<SocioResponseDTO> {
    return this.api.patch<SocioResponseDTO, null>(`${this.basePath}/${id}/estado?estado=${estado}`, null);
  }
}
