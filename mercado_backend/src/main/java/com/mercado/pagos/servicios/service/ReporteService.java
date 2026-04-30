package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.response.ResumenDeudasResponseDTO;
import com.mercado.pagos.servicios.dto.response.ResumenFlujoCajaResponseDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

public interface ReporteService {

    ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorDia(LocalDate fecha);

    ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorMes(YearMonth periodo);

    ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorAnio(int anio);

    ResumenFlujoCajaResponseDTO obtenerResumenFlujoCaja(LocalDateTime inicio, LocalDateTime fin);

    ResumenDeudasResponseDTO obtenerResumenDeudas(LocalDateTime inicio, LocalDateTime fin);
}
