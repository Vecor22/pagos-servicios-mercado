package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.ConceptoCobroRequestDTO;
import com.mercado.pagos.servicios.dto.response.ConceptoCobroResponseDTO;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
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

    @Override
    public ConceptoCobroResponseDTO crearConcepto(ConceptoCobroRequestDTO requestDTO) {
        ConceptoCobro concepto = ConceptoCobro.builder()
                .nombre(requestDTO.getNombre())
                .descripcion(requestDTO.getDescripcion())
                .tipoCobro(parseTipoCobro(requestDTO.getTipoCobro()))
                .build();

        ConceptoCobro guardado = conceptoCobroRepository.save(concepto);
        log.info("Concepto de cobro creado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    @Override
    public ConceptoCobroResponseDTO actualizarConcepto(Long id, ConceptoCobroRequestDTO requestDTO) {
        ConceptoCobro concepto = conceptoCobroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado con id: " + id));

        concepto.setNombre(requestDTO.getNombre());
        concepto.setDescripcion(requestDTO.getDescripcion());
        concepto.setTipoCobro(parseTipoCobro(requestDTO.getTipoCobro()));

        ConceptoCobro actualizado = conceptoCobroRepository.save(concepto);
        log.info("Concepto de cobro actualizado con id {}", actualizado.getId());
        return toResponse(actualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoCobroResponseDTO obtenerConceptoPorId(Long id) {
        ConceptoCobro concepto = conceptoCobroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado con id: " + id));
        return toResponse(concepto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptoCobroResponseDTO> listarConceptos() {
        return conceptoCobroRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ConceptoCobroResponseDTO toResponse(ConceptoCobro concepto) {
        return ConceptoCobroResponseDTO.builder()
                .id(concepto.getId())
                .nombre(concepto.getNombre())
                .descripcion(concepto.getDescripcion())
                .tipoCobro(concepto.getTipoCobro().name())
                .build();
    }

    private TipoCobro parseTipoCobro(String tipoCobro) {
        return TipoCobro.valueOf(tipoCobro.toUpperCase());
    }

}
