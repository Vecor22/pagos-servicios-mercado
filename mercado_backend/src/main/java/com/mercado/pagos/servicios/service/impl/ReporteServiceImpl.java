package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.response.ResumenDeudasResponseDTO;
import com.mercado.pagos.servicios.dto.response.ResumenFlujoCajaResponseDTO;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import com.mercado.pagos.servicios.repository.DeudaRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final BigDecimal CIEN = new BigDecimal("100.00");

    private final PagoRepository pagoRepository;
    private final DeudaRepository deudaRepository;

    @Override
    public ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorDia(LocalDate fecha) {
        return obtenerResumenFlujoCaja(fecha.atStartOfDay(), finDelDia(fecha));
    }

    @Override
    public ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorMes(YearMonth periodo) {
        return obtenerResumenFlujoCaja(periodo.atDay(1).atStartOfDay(), finDelDia(periodo.atEndOfMonth()));
    }

    @Override
    public ResumenFlujoCajaResponseDTO obtenerResumenFlujoCajaPorAnio(int anio) {
        return obtenerResumenFlujoCaja(LocalDate.of(anio, 1, 1).atStartOfDay(), finDelDia(LocalDate.of(anio, 12, 31)));
    }

    @Override
    public ResumenFlujoCajaResponseDTO obtenerResumenFlujoCaja(LocalDateTime inicio, LocalDateTime fin) {
        List<Pago> pagos = pagoRepository.findByEstadoAndFechaPagoBetween(EstadoPago.REGISTRADO, inicio, fin);

        return ResumenFlujoCajaResponseDTO.builder()
                .inicio(inicio)
                .fin(fin)
                .totalIngresos(sumarPagos(pagos))
                .cantidadPagos(pagos.size())
                .build();
    }

    @Override
    public ResumenDeudasResponseDTO obtenerResumenDeudas(LocalDateTime inicio, LocalDateTime fin) {
        List<Deuda> deudas = deudaRepository.findByFechaGeneracionBetween(inicio, fin);
        return construirResumen(inicio, fin, deudas);
    }

    private ResumenDeudasResponseDTO construirResumen(
            LocalDateTime inicio,
            LocalDateTime fin,
            List<Deuda> deudas
    ) {
        BigDecimal montoTotalPagadas = sumarDeudasPorEstado(deudas, EstadoDeuda.PAGADA);
        BigDecimal montoTotalPendientes = sumarDeudasPorEstado(deudas, EstadoDeuda.PENDIENTE);
        BigDecimal montoTotalPagables = montoTotalPagadas.add(montoTotalPendientes);
        Integer cantidadPagadas = contarDeudasPorEstado(deudas, EstadoDeuda.PAGADA);
        Integer cantidadPendientes = contarDeudasPorEstado(deudas, EstadoDeuda.PENDIENTE);
        Integer cantidadTotalPagables = cantidadPagadas + cantidadPendientes;

        return ResumenDeudasResponseDTO.builder()
                .inicio(inicio)
                .fin(fin)
                .montoTotalPagables(montoTotalPagables)
                .montoTotalPagadas(montoTotalPagadas)
                .montoTotalPendientes(montoTotalPendientes)
                .porcentajePagadas(calcularPorcentaje(BigDecimal.valueOf(cantidadPagadas), BigDecimal.valueOf(cantidadTotalPagables)))
                .porcentajePendientes(calcularPorcentaje(BigDecimal.valueOf(cantidadPendientes), BigDecimal.valueOf(cantidadTotalPagables)))
                .cantidadTotalPagables(cantidadTotalPagables)
                .cantidadPagadas(cantidadPagadas)
                .cantidadPendientes(cantidadPendientes)
                .cantidadExoneradas(contarDeudasPorEstado(deudas, EstadoDeuda.EXONERADA))
                .cantidadDistribuidas(contarDeudasPorEstado(deudas, EstadoDeuda.DISTRIBUIDA))
                .build();
    }

    private BigDecimal sumarPagos(List<Pago> pagos) {
        return pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumarDeudasPorEstado(List<Deuda> deudas, EstadoDeuda estado) {
        return deudas.stream()
                .filter(deuda -> deuda.getEstado() == estado)
                .map(Deuda::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer contarDeudasPorEstado(List<Deuda> deudas, EstadoDeuda estado) {
        return (int) deudas.stream()
                .filter(deuda -> deuda.getEstado() == estado)
                .count();
    }

    private BigDecimal calcularPorcentaje(BigDecimal valor, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return valor.multiply(CIEN).divide(total, 2, RoundingMode.HALF_UP);
    }

    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(23, 59, 59);
    }
}
