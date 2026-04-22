package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioResponseDTO;

import java.util.List;

public interface SocioService {

    SocioResponseDTO crearSocio(SocioRequestDTO requestDTO);

    List<SocioResponseDTO> listarSocios();

    List<SocioResponseDTO> buscarSociosPorNombre(String nombre);

    SocioResponseDTO obtenerSocioPorId(Long id);

    SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO requestDTO);

    SocioResponseDTO cambiarEstadoSocio(Long id, String estado);

}
