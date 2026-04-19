package com.mercado.pagos.servicios.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionPuestoRequestDTO {

    @NotNull
    private Long idSocio;

    @NotNull
    private Long idPuesto;

}
