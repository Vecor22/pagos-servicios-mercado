package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PagoResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoService {

    PagoResponseDTO registrarPago(PagoRequestDTO requestDTO);

    PagoResponseDTO obtenerPagoPorId(Long id);

    PagoResponseDTO obtenerPagoPorDeuda(Long idDeuda);

    List<PagoResponseDTO> listarPagos();

    List<PagoResponseDTO> listarPagosPorCodigoPuesto(String codigoPuesto);

    List<PagoResponseDTO> listarPagosPorFechas(LocalDateTime inicio, LocalDateTime fin);

    PagoResponseDTO anularPago(Long id, String motivo);

}
