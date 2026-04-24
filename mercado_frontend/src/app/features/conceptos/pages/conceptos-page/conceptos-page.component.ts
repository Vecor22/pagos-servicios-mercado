import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiErrorResponse, ConceptoRequestDTO, ConceptoResponseDTO, TipoCobro } from '../../models/concepto.models';
import { ConceptoService } from '../../services/concepto.service';

@Component({
  selector: 'app-conceptos-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './conceptos-page.component.html',
  styleUrl: './conceptos-page.component.css'
})
export class ConceptosPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly conceptoService = inject(ConceptoService);

  readonly tiposCobro: TipoCobro[] = ['FIJO', 'CONSUMO'];
  readonly conceptoForm = this.fb.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(255)]],
    tipoCobro: ['FIJO' as TipoCobro, [Validators.required]]
  });

  conceptos: ConceptoResponseDTO[] = [];
  selectedConceptoId: number | null = null;
  isLoading = false;
  isSaving = false;
  successMessage = '';
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.loadConceptos();
  }

  get isEditing(): boolean {
    return this.selectedConceptoId !== null;
  }

  loadConceptos(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    this.isLoading = true;
    this.conceptoService.listar().pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (conceptos) => {
        this.conceptos = conceptos;
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de conceptos.')
    });
  }

  save(): void {
    this.clearMessages();
    this.conceptoForm.markAllAsTouched();

    if (this.conceptoForm.invalid || this.isSaving) {
      this.errorMessage = 'Revisa los campos marcados antes de guardar.';
      return;
    }

    const request = this.buildRequest();
    const operation = this.isEditing && this.selectedConceptoId !== null
      ? this.conceptoService.actualizar(this.selectedConceptoId, request)
      : this.conceptoService.crear(request);

    this.isSaving = true;
    operation.pipe(
      finalize(() => {
        this.isSaving = false;
      })
    ).subscribe({
      next: () => {
        this.successMessage = this.isEditing
          ? 'Concepto actualizado correctamente.'
          : 'Concepto creado correctamente.';
        this.resetForm();
        this.loadConceptos(false);
      },
      error: (error) => this.handleError(error, 'No se pudo guardar el concepto.')
    });
  }

  edit(concepto: ConceptoResponseDTO): void {
    this.clearMessages();
    this.isLoading = true;

    this.conceptoService.obtenerPorId(concepto.id).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (detail) => {
        this.selectedConceptoId = detail.id;
        this.conceptoForm.setValue({
          nombre: detail.nombre,
          descripcion: detail.descripcion ?? '',
          tipoCobro: detail.tipoCobro
        });
      },
      error: (error) => this.handleError(error, 'No se pudo obtener el concepto seleccionado.')
    });
  }

  resetForm(): void {
    this.selectedConceptoId = null;
    this.fieldErrors = {};
    this.conceptoForm.reset({
      nombre: '',
      descripcion: '',
      tipoCobro: 'FIJO'
    });
  }

  hasControlError(controlName: keyof ConceptoRequestDTO): boolean {
    const control = this.conceptoForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  getControlMessage(controlName: keyof ConceptoRequestDTO): string {
    if (this.fieldErrors[controlName]) {
      return this.fieldErrors[controlName];
    }

    const control = this.conceptoForm.controls[controlName];
    if (control.hasError('required')) {
      return 'Campo obligatorio.';
    }
    if (control.hasError('maxlength')) {
      return 'El valor supera el largo permitido.';
    }

    return '';
  }

  private buildRequest(): ConceptoRequestDTO {
    const raw = this.conceptoForm.getRawValue();
    return {
      nombre: raw.nombre.trim(),
      descripcion: raw.descripcion.trim(),
      tipoCobro: raw.tipoCobro
    };
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
