package com.mercado.pagos.servicios.dto.response;

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
public class SocioResponseDTO {

    private Long id;
    private String codigoSocio;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String correo;
    private String estado;
}
