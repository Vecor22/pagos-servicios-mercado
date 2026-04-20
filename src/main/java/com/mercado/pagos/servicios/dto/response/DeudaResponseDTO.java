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
public class DeudaResponseDTO {

    private Long id;
    private String codigoDeuda;
    private Long idConceptoCobro;
    private String nombreConcepto;
    private Long idPuesto;
    private String codigoPuesto;
    private Long idSocio;
    private String nombreCompletoSocio;
    private BigDecimal monto;
    private String tipoGeneracion;
    private String estado;
    private LocalDateTime fechaGeneracion;
    private String observacion;
}
