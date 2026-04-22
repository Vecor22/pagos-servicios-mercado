package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;
import com.mercado.pagos.servicios.service.SocioPuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/asignaciones")
public class SocioPuestoController {

    private final SocioPuestoService socioPuestoService;

    @PostMapping
    public ResponseEntity<SocioPuestoResponseDTO> asignarPuesto(
            @Valid @RequestBody AsignacionPuestoRequestDTO requestDTO
    ) {
        log.info("Solicitud para asignar puesto {} al socio {}", requestDTO.getIdPuesto(), requestDTO.getIdSocio());
        return ResponseEntity.status(HttpStatus.CREATED).body(socioPuestoService.asignarPuesto(requestDTO));
    }

    @GetMapping("/socio/{idSocio}")
    public ResponseEntity<List<SocioPuestoResponseDTO>> obtenerPuestosPorSocio(@PathVariable Long idSocio) {
        log.info("Solicitud para obtener puestos del socio {}", idSocio);
        return ResponseEntity.ok(socioPuestoService.obtenerPuestosPorSocio(idSocio));
    }

    @PatchMapping("/puesto/{idPuesto}/reasignar")
    public ResponseEntity<SocioPuestoResponseDTO> reasignarPuesto(
            @PathVariable Long idPuesto,
            @RequestParam Long idSocio
    ) {
        log.info("Solicitud para reasignar puesto {} al socio {}", idPuesto, idSocio);
        return ResponseEntity.ok(socioPuestoService.reasignarPuesto(idPuesto, idSocio));
    }

}
