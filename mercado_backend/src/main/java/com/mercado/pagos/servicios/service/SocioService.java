package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.SocioResponseDTO;

public interface SocioService {

    SocioResponseDTO crearSocio(SocioRequestDTO requestDTO);

    PageResponseDTO<SocioResponseDTO> listarSocios(int page, int size);

    PageResponseDTO<SocioResponseDTO> buscarSocios(String termino, int page, int size);

    SocioResponseDTO obtenerSocioPorId(Long id);

    SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO requestDTO);

    SocioResponseDTO cambiarEstadoSocio(Long id, String estado);

}
