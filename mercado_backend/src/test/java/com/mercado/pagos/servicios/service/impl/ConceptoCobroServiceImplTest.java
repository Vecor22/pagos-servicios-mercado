package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.response.ConceptoCobroResponseDTO;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.enums.TipoCobro;
import com.mercado.pagos.servicios.repository.ConceptoCobroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConceptoCobroServiceImplTest {

    @Mock
    private ConceptoCobroRepository conceptoCobroRepository;

    @InjectMocks
    private ConceptoCobroServiceImpl conceptoCobroService;

    @Test
    void obtenerConceptoPorIdRetornaConceptoExistente() {
        ConceptoCobro concepto = ConceptoCobro.builder()
                .id(1L)
                .nombre("Mantenimiento")
                .descripcion("Cobro mensual")
                .tipoCobro(TipoCobro.FIJO)
                .build();

        when(conceptoCobroRepository.findById(1L)).thenReturn(Optional.of(concepto));

        ConceptoCobroResponseDTO response = conceptoCobroService.obtenerConceptoPorId(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNombre()).isEqualTo("Mantenimiento");
        assertThat(response.getDescripcion()).isEqualTo("Cobro mensual");
        assertThat(response.getTipoCobro()).isEqualTo("FIJO");
    }

    @Test
    void obtenerConceptoPorIdLanzaExcepcionCuandoNoExiste() {
        when(conceptoCobroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> conceptoCobroService.obtenerConceptoPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Concepto de cobro no encontrado con id: 99");
    }

}
