package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;

import java.util.List;

public interface SocioPuestoService {

    SocioPuestoResponseDTO asignarPuesto(AsignacionPuestoRequestDTO requestDTO);

    List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio);

}
