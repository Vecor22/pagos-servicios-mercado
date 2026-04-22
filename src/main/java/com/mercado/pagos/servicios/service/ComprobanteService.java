package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.response.ComprobanteResponseDTO;

public interface ComprobanteService {

    ComprobanteResponseDTO obtenerComprobantePorPago(Long idPago);

}
