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
public class PagoResponseDTO {

    private Long id;
    private String codigoPago;
    private Long idDeuda;
    private String codigoDeuda;
    private BigDecimal montoPagado;
    private String medioPago;
    private String numeroOperacion;
    private LocalDateTime fechaPago;
    private String estado;
}
