package com.mercado.pagos.servicios.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeudaRequestDTO {

    @NotNull
    private Long idConceptoCobro;

    @NotNull
    private Long idPuesto;

    @NotNull
    private Long idSocio;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal monto;

    @NotNull
    @Size(max = 20)
    private String tipoGeneracion;

    @Size(max = 255)
    private String observacion;
}
