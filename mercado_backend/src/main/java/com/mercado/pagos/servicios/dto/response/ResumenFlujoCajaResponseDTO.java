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
public class ResumenFlujoCajaResponseDTO {

    private LocalDateTime inicio;
    private LocalDateTime fin;
    private BigDecimal totalIngresos;
    private Integer cantidadPagos;
}
