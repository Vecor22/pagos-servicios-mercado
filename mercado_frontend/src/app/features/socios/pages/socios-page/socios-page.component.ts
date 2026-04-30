import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiErrorResponse, EstadoSocio, SocioRequestDTO, SocioResponseDTO } from '../../models/socio.models';
import { SocioService } from '../../services/socio.service';

@Component({
  selector: 'app-socios-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './socios-page.component.html',
  styleUrl: './socios-page.component.css'
})
export class SociosPageComponent implements OnInit {
  private readonly pageSize = 10;
  private readonly fb = inject(FormBuilder);
  private readonly socioService = inject(SocioService);

  readonly estados: EstadoSocio[] = ['ACTIVO', 'INACTIVO'];
  readonly searchForm = this.fb.nonNullable.group({
    termino: ['']
  });
  readonly socioForm = this.fb.nonNullable.group({
    nombres: ['', [Validators.required, Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.maxLength(100)]],
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    telefono: ['', [Validators.required, Validators.maxLength(20)]],
    correo: ['', [Validators.email, Validators.maxLength(100)]],
    estado: ['ACTIVO' as EstadoSocio, [Validators.required]]
  });

  socios: SocioResponseDTO[] = [];
  totalSocios = 0;
  currentPage = 0;
  totalPages = 0;
  selectedSocioId: number | null = null;
  isLoading = false;
  isSaving = false;
  isChangingState = false;
  successMessage = '';
  errorMessage = '';
  fieldErrors: Record<string, string> = {};

  // --- NUEVA VARIABLE PARA CONTROLAR EL PANEL ---
  mostrarPanelFormulario = false;

  ngOnInit(): void {
    this.loadSocios();
  }

  get isEditing(): boolean {
    return this.selectedSocioId !== null;
  }

  get hasSearchTerm(): boolean {
    return this.searchTerm.length > 0;
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  private get searchTerm(): string {
    return this.searchForm.controls.termino.value.trim();
  }

  loadSocios(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }
    this.isLoading = true;

    this.socioService.listar(this.currentPage, this.pageSize).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.updatePageState(response);
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de socios.')
    });
  }

  search(clearFeedback = true): void {
    this.currentPage = 0;
    this.searchByCurrentFilter(clearFeedback);
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage || this.isLoading) {
      return;
    }

    this.currentPage = page;
    this.searchByCurrentFilter(false);
  }

  private searchByCurrentFilter(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    if (!this.hasSearchTerm) {
      this.loadSocios();
      return;
    }

    this.isLoading = true;
    this.socioService.buscar(this.searchTerm, this.currentPage, this.pageSize).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.updatePageState(response);
        if (response.totalElements === 0) {
          this.successMessage = 'No se encontraron socios con ese nombre o DNI.';
        }
      },
      error: (error) => this.handleError(error, 'No se pudo realizar la busqueda.')
    });
  }

  clearSearch(): void {
    this.searchForm.reset();
    this.currentPage = 0;
    this.loadSocios();
  }

  save(): void {
    this.clearMessages();
    this.socioForm.markAllAsTouched();

    if (this.socioForm.invalid || this.isSaving) {
      this.errorMessage = 'Revisa los campos marcados antes de guardar.';
      return;
    }

    const request = this.buildRequest();
    const operation = this.isEditing && this.selectedSocioId !== null
      ? this.socioService.actualizar(this.selectedSocioId, request)
      : this.socioService.crear(request);

    this.isSaving = true;
    operation.pipe(
      finalize(() => {
        this.isSaving = false;
      })
    ).subscribe({
      next: () => {
        this.successMessage = this.isEditing ? 'Socio actualizado correctamente.' : 'Socio creado correctamente.';
        // Ocultamos el panel después de guardar con éxito
        this.mostrarPanelFormulario = false;
        this.resetForm();
        this.refreshAfterChange();
      },
      error: (error) => this.handleError(error, 'No se pudo guardar el socio.')
    });
  }

  edit(socio: SocioResponseDTO): void {
    this.clearMessages();
    this.isLoading = true;

    this.socioService.obtenerPorId(socio.id).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (detail) => {
        this.selectedSocioId = detail.id;
        this.socioForm.setValue({
          nombres: detail.nombres,
          apellidos: detail.apellidos,
          dni: detail.dni,
          telefono: detail.telefono,
          correo: detail.correo ?? '',
          estado: detail.estado
        });
        // Abrimos el panel al editar
        this.mostrarPanelFormulario = true;
      },
      error: (error) => this.handleError(error, 'No se pudo obtener el socio seleccionado.')
    });
  }

  toggleEstado(socio: SocioResponseDTO): void {
    this.clearMessages();
    const nuevoEstado: EstadoSocio = socio.estado === 'ACTIVO' ? 'INACTIVO' : 'ACTIVO';
    this.isChangingState = true;

    this.socioService.cambiarEstado(socio.id, nuevoEstado).pipe(
      finalize(() => {
        this.isChangingState = false;
      })
    ).subscribe({
      next: () => {
        this.successMessage = `Estado cambiado a ${nuevoEstado}.`;
        this.refreshAfterChange();
      },
      error: (error) => this.handleError(error, 'No se pudo cambiar el estado del socio.')
    });
  }

  resetForm(): void {
    this.selectedSocioId = null;
    this.fieldErrors = {};
    this.socioForm.reset({
      nombres: '',
      apellidos: '',
      dni: '',
      telefono: '',
      correo: '',
      estado: 'ACTIVO'
    });
    // Al resetear (botón Nuevo Socio), abrimos el panel
    this.mostrarPanelFormulario = true;
  }

  // --- NUEVO MÉTODO PARA CANCELAR ---
  cancelar(): void {
    this.mostrarPanelFormulario = false;
    this.selectedSocioId = null;
    this.socioForm.reset();
  }

  hasControlError(controlName: keyof SocioRequestDTO): boolean {
    const control = this.socioForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  getControlMessage(controlName: keyof SocioRequestDTO): string {
    if (this.fieldErrors[controlName]) {
      return this.fieldErrors[controlName];
    }

    const control = this.socioForm.controls[controlName];
    if (control.hasError('required')) {
      return 'Campo obligatorio.';
    }
    if (control.hasError('email')) {
      return 'Ingresa un correo valido.';
    }
    if (control.hasError('pattern')) {
      return 'El DNI debe tener 8 digitos.';
    }
    if (control.hasError('maxlength')) {
      return 'El valor supera el largo permitido.';
    }

    return '';
  }

  private refreshAfterChange(): void {
    if (this.hasSearchTerm) {
      this.searchByCurrentFilter(false);
    } else {
      this.loadSocios(false);
    }
  }

  private buildRequest(): SocioRequestDTO {
    const raw = this.socioForm.getRawValue();
    return {
      nombres: raw.nombres.trim(),
      apellidos: raw.apellidos.trim(),
      dni: raw.dni.trim(),
      telefono: raw.telefono.trim(),
      correo: raw.correo.trim(),
      estado: raw.estado
    };
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage = '';
    this.fieldErrors = {};
  }

  private updatePageState(response: { content: SocioResponseDTO[]; page: number; totalElements: number; totalPages: number }): void {
    this.socios = response.content;
    this.currentPage = response.page;
    this.totalSocios = response.totalElements;
    this.totalPages = response.totalPages;
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
