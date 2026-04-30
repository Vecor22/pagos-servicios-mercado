import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, finalize } from 'rxjs';
import {
  ApiErrorResponse,
  ResumenDeudasResponseDTO,
  ResumenFlujoCajaResponseDTO
} from '../../models/reporte.models';
import { ReporteService } from '../../services/reporte.service';

@Component({
  selector: 'app-reportes-page',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './reportes-page.component.html',
  styleUrl: './reportes-page.component.css'
})
export class ReportesPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly reporteService = inject(ReporteService);

  readonly flujoDiaForm = this.fb.nonNullable.group({
    fecha: ['', [Validators.required]]
  });
  readonly flujoMesForm = this.fb.nonNullable.group({
    anio: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]],
    mes: [new Date().getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]]
  });
  readonly flujoAnioForm = this.fb.nonNullable.group({
    anio: [new Date().getFullYear(), [Validators.required, Validators.min(2000)]]
  });
  readonly flujoRangoForm = this.fb.nonNullable.group({
    inicio: ['', [Validators.required]],
    fin: ['', [Validators.required]]
  });
  readonly deudasRangoForm = this.fb.nonNullable.group({
    inicio: ['', [Validators.required]],
    fin: ['', [Validators.required]]
  });

  flujoCaja: ResumenFlujoCajaResponseDTO | null = null;
  resumenDeudas: ResumenDeudasResponseDTO | null = null;
  flujoTitulo = '';
  deudasTitulo = '';
  isLoadingFlujo = false;
  isLoadingDeudas = false;
  successMessage = '';
  errorMessage = '';

  consultarFlujoDia(): void {
    this.clearMessages();
    this.flujoDiaForm.markAllAsTouched();
    const fecha = this.toBackendDate(this.flujoDiaForm.controls.fecha.value);

    if (!fecha) {
      this.errorMessage = 'Selecciona una fecha para consultar el flujo de caja diario.';
      return;
    }

    this.executeFlujo(() => this.reporteService.flujoCajaPorDia(fecha), `Flujo de caja del dia ${fecha}`);
  }

  consultarFlujoMes(): void {
    this.clearMessages();
    this.flujoMesForm.markAllAsTouched();

    if (this.flujoMesForm.invalid) {
      this.errorMessage = 'Ingresa un anio y mes validos.';
      return;
    }

    const anio = Number(this.flujoMesForm.controls.anio.value);
    const mes = Number(this.flujoMesForm.controls.mes.value);
    this.executeFlujo(() => this.reporteService.flujoCajaPorMes(anio, mes), `Flujo de caja mensual ${mes}/${anio}`);
  }

  consultarFlujoAnio(): void {
    this.clearMessages();
    this.flujoAnioForm.markAllAsTouched();

    if (this.flujoAnioForm.invalid) {
      this.errorMessage = 'Ingresa un anio valido.';
      return;
    }

    const anio = Number(this.flujoAnioForm.controls.anio.value);
    this.executeFlujo(() => this.reporteService.flujoCajaPorAnio(anio), `Flujo de caja anual ${anio}`);
  }

  consultarFlujoRango(): void {
    this.clearMessages();
    this.flujoRangoForm.markAllAsTouched();
    const inicio = this.toBackendDate(this.flujoRangoForm.controls.inicio.value);
    const fin = this.toBackendDate(this.flujoRangoForm.controls.fin.value);

    if (!inicio || !fin) {
      this.errorMessage = 'Selecciona fecha de inicio y fin para el flujo de caja.';
      return;
    }

    this.executeFlujo(() => this.reporteService.flujoCajaPorFechas(inicio, fin), `Flujo de caja del ${inicio} al ${fin}`);
  }

  consultarDeudasRango(): void {
    this.clearMessages();
    this.deudasRangoForm.markAllAsTouched();
    const inicio = this.toBackendDate(this.deudasRangoForm.controls.inicio.value);
    const fin = this.toBackendDate(this.deudasRangoForm.controls.fin.value);

    if (!inicio || !fin) {
      this.errorMessage = 'Selecciona fecha de inicio y fin para el resumen de deudas.';
      return;
    }

    this.isLoadingDeudas = true;
    this.reporteService.resumenDeudasPorFechas(inicio, fin).pipe(
      finalize(() => {
        this.isLoadingDeudas = false;
      })
    ).subscribe({
      next: (resumen) => {
        this.resumenDeudas = resumen;
        this.deudasTitulo = `Resumen de deudas del ${inicio} al ${fin}`;
        this.successMessage = 'Resumen de deudas generado correctamente.';
      },
      error: (error) => this.handleError(error, 'No se pudo generar el resumen de deudas.')
    });
  }

  formatMoney(value: number | null | undefined): string {
    return new Intl.NumberFormat('es-PE', {
      style: 'currency',
      currency: 'PEN'
    }).format(Number(value ?? 0));
  }

  formatPercent(value: number | null | undefined): string {
    return `${Number(value ?? 0).toFixed(2)}%`;
  }

  private executeFlujo(request: () => Observable<ResumenFlujoCajaResponseDTO>, title: string): void {
    this.isLoadingFlujo = true;
    request().pipe(
      finalize(() => {
        this.isLoadingFlujo = false;
      })
    ).subscribe({
      next: (resumen) => {
        this.flujoCaja = resumen;
        this.flujoTitulo = title;
        this.successMessage = 'Resumen de flujo de caja generado correctamente.';
      },
      error: (error) => this.handleError(error, 'No se pudo generar el resumen de flujo de caja.')
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
