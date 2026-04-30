package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;

public interface PuestoService {

    PuestoResponseDTO crearPuesto();

    PageResponseDTO<PuestoResponseDTO> listarPuestos(boolean sinAsignacion, int page, int size);

}
