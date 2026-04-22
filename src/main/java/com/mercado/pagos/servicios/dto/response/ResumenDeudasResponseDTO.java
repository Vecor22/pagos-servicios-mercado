package com.mercado.pagos.servicios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenDeudasResponseDTO {

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private BigDecimal totalDeuda;
    private BigDecimal totalPagado;
    private BigDecimal totalPendiente;
    private BigDecimal totalExonerado;
    private BigDecimal totalActualCaja;
    private BigDecimal porcentajePagado;
    private BigDecimal porcentajePendiente;
    private BigDecimal porcentajeExonerado;
    private Integer cantidadDeudas;
    private Integer cantidadPagadas;
    private Integer cantidadPendientes;
    private Integer cantidadExoneradas;
    private Integer cantidadDistribuidas;
}
