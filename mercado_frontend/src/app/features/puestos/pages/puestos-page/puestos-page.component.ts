import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiErrorResponse, PuestoResponseDTO } from '../../models/puesto.models';
import { PuestoService } from '../../services/puesto.service';

@Component({
  selector: 'app-puestos-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './puestos-page.component.html',
  styleUrl: './puestos-page.component.css'
})
export class PuestosPageComponent implements OnInit {
  private readonly pageSize = 10;
  private readonly puestoService = inject(PuestoService);
  showOnlySinAsignacion = false;

  puestos: PuestoResponseDTO[] = [];
  totalPuestos = 0;
  currentPage = 0;
  totalPages = 0;
  isLoading = false;
  isCreating = false;
  successMessage = '';
  errorMessage = '';

  ngOnInit(): void {
    this.loadPuestos();
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, index) => index);
  }

  loadPuestos(clearFeedback = true): void {
    if (clearFeedback) {
      this.clearMessages();
    }

    this.isLoading = true;
    this.puestoService.listar(this.currentPage, this.pageSize, this.showOnlySinAsignacion).pipe(
      finalize(() => {
        this.isLoading = false;
      })
    ).subscribe({
      next: (response) => {
        this.puestos = response.content;
        this.totalPuestos = response.totalElements;
        this.currentPage = response.page;
        this.totalPages = response.totalPages;
      },
      error: (error) => this.handleError(error, 'No se pudo cargar el listado de puestos.')
    });
  }

  createPuesto(): void {
    this.clearMessages();
    this.isCreating = true;

    this.puestoService.crear().pipe(
      finalize(() => {
        this.isCreating = false;
      })
    ).subscribe({
      next: (puesto) => {
        this.successMessage = `Puesto ${puesto.codigoPuesto} creado correctamente.`;
        this.currentPage = 0;
        this.loadPuestos(false);
      },
      error: (error) => this.handleError(error, 'No se pudo crear el puesto.')
    });
  }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages || page === this.currentPage || this.isLoading) {
      return;
    }

    this.currentPage = page;
    this.loadPuestos(false);
  }

  toggleSinAsignacion(): void {
    this.showOnlySinAsignacion = !this.showOnlySinAsignacion;
    this.currentPage = 0;
    this.loadPuestos();
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
