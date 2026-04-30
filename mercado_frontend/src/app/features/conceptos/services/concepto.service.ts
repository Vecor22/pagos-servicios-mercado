import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ConceptoRequestDTO, ConceptoResponseDTO } from '../models/concepto.models';

@Injectable({
  providedIn: 'root'
})
export class ConceptoService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/conceptos';

  listar(): Observable<ConceptoResponseDTO[]> {
    return this.api.get<ConceptoResponseDTO[]>(this.basePath);
  }

  obtenerPorId(id: number): Observable<ConceptoResponseDTO> {
    return this.api.get<ConceptoResponseDTO>(`${this.basePath}/${id}`);
  }

  crear(request: ConceptoRequestDTO): Observable<ConceptoResponseDTO> {
    return this.api.post<ConceptoResponseDTO, ConceptoRequestDTO>(this.basePath, request);
  }

  actualizar(id: number, request: ConceptoRequestDTO): Observable<ConceptoResponseDTO> {
    return this.api.put<ConceptoResponseDTO, ConceptoRequestDTO>(`${this.basePath}/${id}`, request);
  }
}
