package com.mercado.pagos.servicios.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionPuestoRequestDTO {

    @NotBlank
    private String codigoSocio;

    @NotBlank
    private String codigoPuesto;

}
