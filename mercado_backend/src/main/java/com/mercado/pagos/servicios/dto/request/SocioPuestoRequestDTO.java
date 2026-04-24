package com.mercado.pagos.servicios.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocioPuestoRequestDTO {

    @NotNull
    private Long idSocio;

    @NotNull
    private Long idPuesto;
}
