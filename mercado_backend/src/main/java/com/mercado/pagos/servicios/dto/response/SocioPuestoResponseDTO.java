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
    private String codigoSocio;
    private String dniSocio;
    private String nombreCompletoSocio;
    private String codigoPuesto;
    private LocalDate fechaAsignacion;
}
