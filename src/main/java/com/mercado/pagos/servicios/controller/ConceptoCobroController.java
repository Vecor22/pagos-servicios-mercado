package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.ConceptoCobroRequestDTO;
import com.mercado.pagos.servicios.dto.response.ConceptoCobroResponseDTO;
import com.mercado.pagos.servicios.service.ConceptoCobroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/conceptos")
public class ConceptoCobroController {

    private final ConceptoCobroService conceptoCobroService;

    @PostMapping
    public ResponseEntity<ConceptoCobroResponseDTO> crearConcepto(
            @Valid @RequestBody ConceptoCobroRequestDTO requestDTO
    ) {
        log.info("Solicitud para crear concepto de cobro {}", requestDTO.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoCobroService.crearConcepto(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<ConceptoCobroResponseDTO>> listarConceptos() {
        log.info("Solicitud para listar conceptos de cobro");
        return ResponseEntity.ok(conceptoCobroService.listarConceptos());
    }

}
