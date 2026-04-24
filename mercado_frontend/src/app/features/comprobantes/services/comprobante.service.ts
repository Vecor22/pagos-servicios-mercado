import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { ComprobanteResponseDTO } from '../models/comprobante.models';

@Injectable({
  providedIn: 'root'
})
export class ComprobanteService {
  private readonly api = inject(ApiService);
  private readonly basePath = '/comprobantes';

  obtenerPorPago(idPago: number): Observable<ComprobanteResponseDTO> {
    return this.api.get<ComprobanteResponseDTO>(`${this.basePath}/pago/${idPago}`);
  }
}
