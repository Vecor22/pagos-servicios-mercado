package com.mercado.pagos.servicios.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResponseDTO {

    private Long id;
    private Long idPago;
    private String numeroComprobante;
    private String tipoComprobante;
    private LocalDateTime fechaEmision;
}
