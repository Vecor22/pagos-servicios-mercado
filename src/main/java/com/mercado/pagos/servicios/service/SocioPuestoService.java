package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;

import java.util.List;

public interface SocioPuestoService {

    SocioPuestoResponseDTO asignarPuesto(AsignacionPuestoRequestDTO requestDTO);

    SocioPuestoResponseDTO reasignarPuesto(Long idPuesto, Long idSocio);

    List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio);

}
