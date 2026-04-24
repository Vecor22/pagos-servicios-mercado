package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.PagoResponseDTO;

import java.time.LocalDateTime;

public interface PagoService {

    PagoResponseDTO registrarPago(PagoRequestDTO requestDTO);

    PagoResponseDTO obtenerPagoPorId(Long id);

    PagoResponseDTO obtenerPagoPorDeuda(Long idDeuda);

    PageResponseDTO<PagoResponseDTO> listarPagos(
            String codigoPago,
            String codigoDeuda,
            LocalDateTime inicio,
            LocalDateTime fin,
            int page,
            int size
    );

    PagoResponseDTO anularPago(Long id, String motivo);

}
