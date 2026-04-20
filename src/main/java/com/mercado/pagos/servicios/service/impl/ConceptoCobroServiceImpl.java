package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.request.ConceptoCobroRequestDTO;
import com.mercado.pagos.servicios.dto.response.ConceptoCobroResponseDTO;
import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.enums.TipoCobro;
import com.mercado.pagos.servicios.repository.ConceptoCobroRepository;
import com.mercado.pagos.servicios.service.ConceptoCobroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConceptoCobroServiceImpl implements ConceptoCobroService {

    private final ConceptoCobroRepository conceptoCobroRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ConceptoCobroResponseDTO crearConcepto(ConceptoCobroRequestDTO requestDTO) {
        ConceptoCobro concepto = objectMapper.convertValue(requestDTO, ConceptoCobro.class);
        concepto.setId(null);
        concepto.setTipoCobro(TipoCobro.valueOf(requestDTO.getTipoCobro().toUpperCase()));

        ConceptoCobro guardado = conceptoCobroRepository.save(concepto);
        log.info("Concepto de cobro creado con id {}", guardado.getId());
        return objectMapper.convertValue(guardado, ConceptoCobroResponseDTO.class);
    }

    @Override
    public List<ConceptoCobroResponseDTO> listarConceptos() {
        return conceptoCobroRepository.findAll().stream()
                .map(concepto -> objectMapper.convertValue(concepto, ConceptoCobroResponseDTO.class))
                .toList();
    }

}
