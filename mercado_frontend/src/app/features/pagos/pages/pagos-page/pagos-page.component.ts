import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ComprobanteResponseDTO } from '../../../comprobantes/models/comprobante.models';
import { ComprobanteService } from '../../../comprobantes/services/comprobante.service';
import { ApiErrorResponse, PagoRequestDTO, PagoResponseDTO } from '../../models/pago.models';
import { PagoService } from '../../services/pago.service';

@Component({
  selector: 'app-pagos-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './pagos-page.component.html',
  styleUrl: './pagos-page.component.css'
})
export class PagosPageComponent implements OnInit {
  private readonly pageSize = 10;
  private readonly fb = inject(FormBuilder);
  private readonly comprobanteService = inject(ComprobanteService);
  private readonly pagoService = inject(PagoService);

  readonly pagoForm = this.fb.nonNullable.group({
    codigoDeuda: ['', [Validators.required, Validators.maxLength(20)]],
    montoPagado: [null as number | null, [Validators.required, Validators.min(0.01)]],
    medioPago: ['', [Validators.required, Validators.maxLength(30)]],
    numeroOperacion: ['', [Validators.maxLength(50)]]
  });
  readonly filtrosForm = this.fb.nonNullable.group({
    codigoPago: [''],
    codigoDeuda: [''],
    inicio: [''],
    fin: ['']
  });
  readonly anularForm = this.fb.nonNullable.group({
    id: [null as number | null, [Validators.required, Validators.min(1)]],
    motivo: ['', [Validators.required, Validators.maxLength(255)]]
  });

  pagos: PagoResponseDTO[] = [];
  selectedPago: PagoResponseDTO | null = null;
  selectedDetailPago: PagoResponseDTO | null = null;
  selectedComprobante: ComprobanteResponseDTO | null = null;
  totalPagos = 0;
  currentPage = 0;
  totalPages = 0;
  isLoading = false;
  isSaving = false;
  isCancelling = false;
  isLoadingComprobante = false;
  successMessage = '';
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.loadPagos();
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  loadPagos(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    this.isLoading = true;
    this.pagoService.listar(this.currentPage, this.pageSize, this.buildFilterParams()).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.pagos = response.content;
        this.totalPagos = response.totalElements;
        this.currentPage = response.page;
        this.totalPages = response.totalPages;
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de pagos.')
    });
  }

  registrar(): void {
    this.clearMessages();
    this.pagoForm.markAllAsTouched();

    if (this.pagoForm.invalid || this.isSaving) {
      this.errorMessage = 'Revisa los campos marcados antes de registrar el pago.';
      return;
    }

    this.isSaving = true;
    this.pagoService.registrar(this.buildRequest()).pipe(
      finalize(() => {
        this.isSaving = false;
      })
    ).subscribe({
      next: (pago) => {
        this.successMessage = `Pago ${pago.codigoPago} registrado correctamente.`;
        this.resetPagoForm();
        this.currentPage = 0;
        this.loadPagos(false);
      },
      error: (error) => this.handleError(error, 'No se pudo registrar el pago.')
    });
  }

  filtrar(): void {
    this.clearMessages();
    const { inicio, fin } = this.filtrosForm.getRawValue();
    if ((inicio && !fin) || (!inicio && fin)) {
      this.errorMessage = 'Si ingresas fechas, debes completar inicio y fin.';
      return;
    }

    this.currentPage = 0;
    this.loadPagos(false);
  }

  clearFilters(): void {
    this.filtrosForm.reset({
      codigoPago: '',
      codigoDeuda: '',
      inicio: '',
      fin: ''
    });
    this.currentPage = 0;
    this.loadPagos();
  }

  openAnular(pago: PagoResponseDTO): void {
    this.clearMessages();
    this.selectedPago = pago;
    this.anularForm.reset({
      id: pago.id,
      motivo: ''
    });
  }

  cancelAnular(): void {
    this.selectedPago = null;
    this.anularForm.reset({
      id: null,
      motivo: ''
    });
  }

  openDetail(pago: PagoResponseDTO): void {
    this.selectedDetailPago = pago;
  }

  closeDetail(): void {
    this.selectedDetailPago = null;
  }

  openComprobante(pago: PagoResponseDTO): void {
    this.clearMessages();
    this.isLoadingComprobante = true;
    this.selectedComprobante = null;

    this.comprobanteService.obtenerPorPago(pago.id).pipe(
      finalize(() => {
        this.isLoadingComprobante = false;
      })
    ).subscribe({
      next: (comprobante) => {
        this.selectedComprobante = comprobante;
      },
      error: (error) => this.handleError(error, 'No se pudo obtener el comprobante del pago.')
    });
  }

  closeComprobante(): void {
    this.selectedComprobante = null;
    this.isLoadingComprobante = false;
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage || this.isLoading) {
      return;
    }

    this.currentPage = page;
    this.loadPagos(false);
  }

  anular(): void {
    this.clearMessages();
    this.anularForm.markAllAsTouched();

    if (this.anularForm.invalid || this.isCancelling) {
      this.errorMessage = 'Ingresa el motivo de anulacion.';
      return;
    }

    const id = Number(this.anularForm.controls.id.value);
    const motivo = this.anularForm.controls.motivo.value.trim();
    this.isCancelling = true;

    this.pagoService.anular(id, motivo).pipe(
      finalize(() => {
        this.isCancelling = false;
      })
    ).subscribe({
      next: (pago) => {
        this.successMessage = `Pago ${pago.codigoPago} anulado correctamente.`;
        this.cancelAnular();
        this.loadPagos(false);
      },
      error: (error) => this.handleError(error, 'No se pudo anular el pago.')
    });
  }

  resetPagoForm(): void {
    this.fieldErrors = {};
    this.pagoForm.reset({
      codigoDeuda: '',
      montoPagado: null,
      medioPago: '',
      numeroOperacion: ''
    });
  }

  hasPagoError(controlName: keyof PagoRequestDTO): boolean {
    const control = this.pagoForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  getFieldMessage(field: keyof PagoRequestDTO): string {
    if (this.fieldErrors[field]) {
      return this.fieldErrors[field];
    }

    const control = this.pagoForm.controls[field];
    if (control.hasError('required')) {
      return 'Campo obligatorio.';
    }
    if (control.hasError('min')) {
      return 'Debe ser mayor a cero.';
    }
    if (control.hasError('maxlength')) {
      return 'El valor supera el largo permitido.';
    }

    return '';
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN'
    }).format(value);
  }

  private buildRequest(): PagoRequestDTO {
    const raw = this.pagoForm.getRawValue();
    return {
      codigoDeuda: raw.codigoDeuda.trim(),
      montoPagado: Number(raw.montoPagado),
      medioPago: raw.medioPago.trim(),
      numeroOperacion: raw.numeroOperacion.trim()
    };
  }

  private buildFilterParams(): { codigoPago?: string; codigoDeuda?: string; inicio?: string; fin?: string } {
    const raw = this.filtrosForm.getRawValue();
    return {
      codigoPago: raw.codigoPago.trim(),
      codigoDeuda: raw.codigoDeuda.trim(),
      inicio: this.toBackendDate(raw.inicio),
      fin: this.toBackendDate(raw.fin)
    };
  }

  private toBackendDate(value: string): string {
    if (!value) {
      return '';
    }

    const [year, month, day] = value.split('-');
    return `${day}-${month}-${year}`;
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.fieldErrors = {};
  }

  private handleError(error: unknown, fallbackMessage: string): void {
    const response = error instanceof HttpErrorResponse ? error.error as ApiErrorResponse | null : null;
    this.fieldErrors = response?.errors ?? {};

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
