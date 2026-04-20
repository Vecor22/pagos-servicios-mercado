package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface DeudaService {

    DeudaResponseDTO crearDeuda(DeudaRequestDTO requestDTO);

    List<DeudaResponseDTO> listarDeudas();

    List<DeudaResponseDTO> listarDeudasPorPuesto(Long idPuesto);

    List<DeudaResponseDTO> listarDeudasPorFechas(LocalDateTime inicio, LocalDateTime fin);

    DeudaResponseDTO exonerarDeuda(Long id, String motivo);

}
