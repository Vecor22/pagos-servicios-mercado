import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ConceptoResponseDTO } from '../../../conceptos/models/concepto.models';
import { ConceptoService } from '../../../conceptos/services/concepto.service';
import {
  ApiErrorResponse,
  DeudaRequestDTO,
  DeudaResponseDTO,
  EstadoDeuda,
  TipoGeneracionDeuda
} from '../../models/deuda.models';
import { DeudaService } from '../../services/deuda.service';

@Component({
  selector: 'app-deudas-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './deudas-page.component.html',
  styleUrl: './deudas-page.component.css'
})
export class DeudasPageComponent implements OnInit {
  private readonly pageSize = 10;
  private readonly fb = inject(FormBuilder);
  private readonly conceptoService = inject(ConceptoService);
  private readonly deudaService = inject(DeudaService);

  readonly tiposGeneracion: TipoGeneracionDeuda[] = ['INDIVIDUAL', 'REPARTIBLE'];
  readonly estados: EstadoDeuda[] = ['PENDIENTE', 'PAGADA', 'EXONERADA', 'DISTRIBUIDA'];
  readonly deudaForm = this.fb.nonNullable.group({
    nombreConceptoCobro: ['', [Validators.required, Validators.maxLength(100)]],
    codigoPuesto: [''],
    monto: [null as number | null, [Validators.required, Validators.min(1)]],
    tipoGeneracion: ['INDIVIDUAL' as TipoGeneracionDeuda, [Validators.required]],
    observacion: ['', [Validators.maxLength(255)]]
  });
  readonly filtrosForm = this.fb.nonNullable.group({
    codigoPuesto: [''],
    estado: ['' as EstadoDeuda | ''],
    inicio: [''],
    fin: ['']
  });
  readonly exonerarForm = this.fb.nonNullable.group({
    id: [null as number | null, [Validators.required, Validators.min(1)]],
    motivo: ['', [Validators.required, Validators.maxLength(255)]]
  });

  conceptos: ConceptoResponseDTO[] = [];
  deudas: DeudaResponseDTO[] = [];
  selectedDeuda: DeudaResponseDTO | null = null;
  selectedDetailDeuda: DeudaResponseDTO | null = null;
  exonerarBlockedMessage = '';
  totalDeudas = 0;
  currentPage = 0;
  totalPages = 0;
  isLoading = false;
  isSaving = false;
  isExonerating = false;
  successMessage = '';
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.loadConceptos();
    this.loadDeudas();
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  loadDeudas(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    this.isLoading = true;
    this.deudaService.listar(this.currentPage, this.pageSize, this.buildFilterParams()).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.deudas = response.content;
        this.totalDeudas = response.totalElements;
        this.currentPage = response.page;
        this.totalPages = response.totalPages;
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de deudas.')
    });
  }

  save(): void {
    this.clearMessages();
    this.deudaForm.markAllAsTouched();

    if (this.deudaForm.invalid || this.isSaving) {
      this.errorMessage = 'Revisa los campos marcados antes de registrar la deuda.';
      return;
    }

    const request = this.buildRequest();
    if (request.tipoGeneracion === 'INDIVIDUAL' && !request.codigoPuesto) {
      this.errorMessage = 'La deuda individual requiere un codigo de puesto.';
      return;
    }

    this.isSaving = true;
    this.deudaService.crear(request).pipe(
      finalize(() => {
        this.isSaving = false;
      })
    ).subscribe({
      next: (deuda) => {
        this.successMessage = `Deuda ${deuda.codigoDeuda} registrada correctamente.`;
        this.resetDeudaForm();
        this.currentPage = 0;
        this.loadDeudas(false);
      },
      error: (error) => this.handleError(error, 'No se pudo registrar la deuda.')
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
    this.loadDeudas(false);
  }

  clearFilters(): void {
    this.filtrosForm.reset({
      codigoPuesto: '',
      estado: '',
      inicio: '',
      fin: ''
    });
    this.currentPage = 0;
    this.loadDeudas();
  }

  openExonerar(deuda: DeudaResponseDTO): void {
    this.clearMessages();
    if (deuda.estado === 'PAGADA') {
      this.exonerarBlockedMessage = 'La deuda ha sido pagada y por lo cual deberá anular el pago para luego poder exonerarla.';
      return;
    }
    this.selectedDeuda = deuda;
    this.exonerarForm.reset({
      id: deuda.id,
      motivo: ''
    });
  }

  openDetail(deuda: DeudaResponseDTO): void {
    this.selectedDetailDeuda = deuda;
  }

  closeDetail(): void {
    this.selectedDetailDeuda = null;
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage || this.isLoading) {
      return;
    }

    this.currentPage = page;
    this.loadDeudas(false);
  }

  cancelExonerar(): void {
    this.selectedDeuda = null;
    this.exonerarForm.reset({
      id: null,
      motivo: ''
    });
  }

  closeExonerarBlockedMessage(): void {
    this.exonerarBlockedMessage = '';
  }

  exonerar(): void {
    this.clearMessages();
    this.exonerarForm.markAllAsTouched();

    if (this.exonerarForm.invalid || this.isExonerating) {
      this.errorMessage = 'Ingresa el motivo de exoneracion.';
      return;
    }

    const id = Number(this.exonerarForm.controls.id.value);
    const motivo = this.exonerarForm.controls.motivo.value.trim();
    this.isExonerating = true;

    this.deudaService.exonerar(id, motivo).pipe(
      finalize(() => {
        this.isExonerating = false;
      })
    ).subscribe({
      next: (deuda) => {
        this.successMessage = `Deuda ${deuda.codigoDeuda} exonerada correctamente.`;
        this.cancelExonerar();
        this.loadDeudas(false);
      },
      error: (error) => this.handleError(error, 'No se pudo exonerar la deuda.')
    });
  }

  resetDeudaForm(): void {
    this.fieldErrors = {};
    this.deudaForm.reset({
      nombreConceptoCobro: '',
      codigoPuesto: '',
      monto: null,
      tipoGeneracion: 'INDIVIDUAL',
      observacion: ''
    });
  }

  hasDeudaError(controlName: keyof DeudaRequestDTO): boolean {
    const control = this.deudaForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  getFieldMessage(field: keyof DeudaRequestDTO): string {
    if (this.fieldErrors[field]) {
      return this.fieldErrors[field];
    }

    const control = this.deudaForm.controls[field];
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

  private buildRequest(): DeudaRequestDTO {
    const raw = this.deudaForm.getRawValue();
    return {
      nombreConceptoCobro: raw.nombreConceptoCobro.trim(),
      codigoPuesto: raw.codigoPuesto.trim() || null,
      monto: Number(raw.monto),
      tipoGeneracion: raw.tipoGeneracion,
      observacion: raw.observacion.trim()
    };
  }

  private buildFilterParams(): { codigoPuesto?: string; estado?: EstadoDeuda | ''; inicio?: string; fin?: string } {
    const raw = this.filtrosForm.getRawValue();
    return {
      codigoPuesto: raw.codigoPuesto.trim(),
      estado: raw.estado,
      inicio: this.toBackendDate(raw.inicio),
      fin: this.toBackendDate(raw.fin)
    };
  }

  private loadConceptos(): void {
    this.conceptoService.listar().subscribe({
      next: (conceptos) => {
        this.conceptos = conceptos;
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el listado de conceptos.';
      }
    });
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
