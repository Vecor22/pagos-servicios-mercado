package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;

import java.util.List;

public interface SocioPuestoService {

    SocioPuestoResponseDTO asignarPuesto(AsignacionPuestoRequestDTO requestDTO);

    SocioPuestoResponseDTO reasignarPuesto(String codigoPuesto, String codigoSocio);

    PageResponseDTO<SocioPuestoResponseDTO> listarAsignaciones(String dniSocio, String nombreSocio, int page, int size);

    List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio);

}
