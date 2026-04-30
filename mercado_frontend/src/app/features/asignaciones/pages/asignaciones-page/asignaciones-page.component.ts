import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiErrorResponse, AsignacionRequestDTO, AsignacionResponseDTO } from '../../models/asignacion.models';
import { AsignacionService } from '../../services/asignacion.service';

@Component({
  selector: 'app-asignaciones-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './asignaciones-page.component.html',
  styleUrl: './asignaciones-page.component.css'
})
export class AsignacionesPageComponent implements OnInit {
  private readonly pageSize = 10;
  private readonly fb = inject(FormBuilder);
  private readonly asignacionService = inject(AsignacionService);

  readonly asignarForm = this.fb.nonNullable.group({
    codigoSocio: ['', [Validators.required, Validators.maxLength(20)]],
    codigoPuesto: ['', [Validators.required, Validators.maxLength(20)]]
  });
  readonly reasignarForm = this.fb.nonNullable.group({
    codigoPuesto: ['', [Validators.required, Validators.maxLength(20)]],
    codigoSocio: ['', [Validators.required, Validators.maxLength(20)]]
  });
  readonly filtrosForm = this.fb.nonNullable.group({
    dniSocio: ['', [Validators.maxLength(8)]],
    nombreSocio: ['', [Validators.maxLength(120)]]
  });

  asignaciones: AsignacionResponseDTO[] = [];
  totalAsignaciones = 0;
  currentPage = 0;
  totalPages = 0;
  isAssigning = false;
  isLoading = false;
  isReassigning = false;
  successMessage = '';
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.loadAsignaciones();
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  loadAsignaciones(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    this.isLoading = true;
    this.asignacionService.listar(this.currentPage, this.pageSize, this.buildFilterParams()).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.asignaciones = response.content;
        this.totalAsignaciones = response.totalElements;
        this.currentPage = response.page;
        this.totalPages = response.totalPages;
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de asignaciones.')
    });
  }

  asignar(): void {
    this.clearMessages();
    this.asignarForm.markAllAsTouched();

    if (this.asignarForm.invalid || this.isAssigning) {
      this.errorMessage = 'Ingresa el codigo del socio y el codigo del puesto antes de asignar.';
      return;
    }

    const request = this.buildAsignacionRequest();
    this.isAssigning = true;

    this.asignacionService.asignar(request).pipe(
      finalize(() => {
        this.isAssigning = false;
      })
    ).subscribe({
      next: (asignacion) => {
        this.successMessage = `Puesto ${asignacion.codigoPuesto} asignado correctamente.`;
        this.asignarForm.reset();
        this.currentPage = 0;
        this.loadAsignaciones(false);
      },
      error: (error) => this.handleError(error, 'No se pudo asignar el puesto.')
    });
  }

  reasignar(): void {
    this.clearMessages();
    this.reasignarForm.markAllAsTouched();

    if (this.reasignarForm.invalid || this.isReassigning) {
      this.errorMessage = 'Ingresa el codigo del puesto y el nuevo codigo de socio.';
      return;
    }

    const codigoPuesto = this.reasignarForm.controls.codigoPuesto.value.trim();
    const codigoSocio = this.reasignarForm.controls.codigoSocio.value.trim();
    this.isReassigning = true;

    this.asignacionService.reasignar(codigoPuesto, codigoSocio).pipe(
      finalize(() => {
        this.isReassigning = false;
      })
    ).subscribe({
      next: (asignacion) => {
        this.successMessage = `Puesto ${asignacion.codigoPuesto} reasignado correctamente.`;
        this.reasignarForm.reset();
        this.currentPage = 0;
        this.loadAsignaciones(false);
      },
      error: (error) => this.handleError(error, 'No se pudo reasignar el puesto.')
    });
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage || this.isLoading) {
      return;
    }

    this.currentPage = page;
    this.loadAsignaciones(false);
  }

  filtrar(): void {
    this.clearMessages();
    this.currentPage = 0;
    this.loadAsignaciones(false);
  }

  clearFilters(): void {
    this.filtrosForm.reset({
      dniSocio: '',
      nombreSocio: ''
    });
    this.currentPage = 0;
    this.loadAsignaciones();
  }

  hasError(formName: 'asignar' | 'reasignar', controlName: string): boolean {
    const control = formName === 'asignar'
      ? this.asignarForm.get(controlName)
      : this.reasignarForm.get(controlName);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  getFieldMessage(field: keyof AsignacionRequestDTO): string {
    if (this.fieldErrors[field]) {
      return this.fieldErrors[field];
    }

    return 'Campo obligatorio.';
  }

  private buildAsignacionRequest(): AsignacionRequestDTO {
    return {
      codigoSocio: this.asignarForm.controls.codigoSocio.value.trim(),
      codigoPuesto: this.asignarForm.controls.codigoPuesto.value.trim()
    };
  }

  private buildFilterParams(): { dniSocio?: string; nombreSocio?: string } {
    const raw = this.filtrosForm.getRawValue();
    return {
      dniSocio: raw.dniSocio.trim(),
      nombreSocio: raw.nombreSocio.trim()
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
