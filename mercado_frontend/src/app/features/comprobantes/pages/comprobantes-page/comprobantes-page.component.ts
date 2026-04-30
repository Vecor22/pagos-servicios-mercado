import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiErrorResponse, ComprobanteResponseDTO } from '../../models/comprobante.models';
import { ComprobanteService } from '../../services/comprobante.service';

@Component({
  selector: 'app-comprobantes-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './comprobantes-page.component.html',
  styleUrl: './comprobantes-page.component.css'
})
export class ComprobantesPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly comprobanteService = inject(ComprobanteService);

  readonly searchForm = this.fb.nonNullable.group({
    idPago: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  comprobante: ComprobanteResponseDTO | null = null;
  isLoading = false;
  successMessage = '';
  errorMessage = '';

  buscar(): void {
    this.clearMessages();
    this.searchForm.markAllAsTouched();

    if (this.searchForm.invalid || this.isLoading) {
      this.errorMessage = 'Ingresa un ID de pago valido.';
      return;
    }

    const idPago = Number(this.searchForm.controls.idPago.value);
    this.isLoading = true;

    this.comprobanteService.obtenerPorPago(idPago).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (comprobante) => {
        this.comprobante = comprobante;
        this.successMessage = 'Comprobante encontrado correctamente.';
      },
      error: (error) => {
        this.comprobante = null;
        this.handleError(error, 'No se pudo obtener el comprobante.');
      }
    });
  }

  limpiar(): void {
    this.searchForm.reset({ idPago: null });
    this.comprobante = null;
    this.clearMessages();
  }

  imprimir(): void {
    window.print();
  }

  hasSearchError(): boolean {
    const control = this.searchForm.controls.idPago;
    return control.invalid && (control.dirty || control.touched);
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
  }

  private handleError(error: unknown, fallbackMessage: string): void {
    const response = error instanceof HttpErrorResponse ? error.error as ApiErrorResponse | null : null;

    if (response?.message) {
      this.errorMessage = this.formatStatusMessage(response.status, response.message);
      return;
    }

    this.errorMessage = fallbackMessage;
  }

  private formatStatusMessage(status: number, message: string): string {
    const labels: Record<number, string> = {
      400: 'Solicitud invalida',
      404: 'No encontrado',
      409: 'Conflicto',
      422: 'Regla de negocio'
    };

    return labels[status] ? `${labels[status]}: ${message}` : message;
  }
}
