package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;

import java.util.List;

public interface PuestoService {

    PuestoResponseDTO crearPuesto();

    PuestoResponseDTO obtenerPuestoPorCodigo(String codigoPuesto);

    List<PuestoResponseDTO> listarPuestos();

    List<PuestoResponseDTO> listarPuestosPorDniSocio(String dni);

}
