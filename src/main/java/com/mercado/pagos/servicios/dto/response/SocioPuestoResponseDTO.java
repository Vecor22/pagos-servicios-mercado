package com.mercado.pagos.servicios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocioPuestoResponseDTO {

    private Long id;
    private Long idSocio;
    private String nombreCompletoSocio;
    private Long idPuesto;
    private String codigoPuesto;
    private LocalDate fechaAsignacion;
}
