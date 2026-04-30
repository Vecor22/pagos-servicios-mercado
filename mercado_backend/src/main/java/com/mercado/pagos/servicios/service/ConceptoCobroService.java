package com.mercado.pagos.servicios.service;

import com.mercado.pagos.servicios.dto.request.ConceptoCobroRequestDTO;
import com.mercado.pagos.servicios.dto.response.ConceptoCobroResponseDTO;

import java.util.List;

public interface ConceptoCobroService {

    ConceptoCobroResponseDTO crearConcepto(ConceptoCobroRequestDTO requestDTO);

    ConceptoCobroResponseDTO actualizarConcepto(Long id, ConceptoCobroRequestDTO requestDTO);

    ConceptoCobroResponseDTO obtenerConceptoPorId(Long id);

    List<ConceptoCobroResponseDTO> listarConceptos();

}
