package com.mercado.pagos.servicios.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
public class PagoRequestDTO {

    @NotBlank
    @Size(max = 20)
    private String codigoDeuda;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal montoPagado;

    @NotBlank
    @Size(max = 30)
    private String medioPago;

    @Size(max = 50)
    private String numeroOperacion;
}
