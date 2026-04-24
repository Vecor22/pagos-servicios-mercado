package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;

import java.time.LocalDateTime;

public interface DeudaService {

    DeudaResponseDTO crearDeuda(DeudaRequestDTO requestDTO);

    PageResponseDTO<DeudaResponseDTO> listarDeudas(int page, int size);

    PageResponseDTO<DeudaResponseDTO> filtrarDeudas(
            String codigoPuesto,
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin,
            int page,
            int size
    );

    DeudaResponseDTO exonerarDeuda(Long id, String motivo);

}
