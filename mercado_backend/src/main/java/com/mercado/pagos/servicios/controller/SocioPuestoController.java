package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
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
        log.info("Solicitud para asignar puesto {} al socio {}", requestDTO.getCodigoPuesto(), requestDTO.getCodigoSocio());
        return ResponseEntity.status(HttpStatus.CREATED).body(socioPuestoService.asignarPuesto(requestDTO));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<SocioPuestoResponseDTO>> listarAsignaciones(
            @RequestParam(required = false) String dniSocio,
            @RequestParam(required = false) String nombreSocio,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para listar asignaciones. dniSocio={}, nombreSocio={}, page={}, size={}",
                dniSocio, nombreSocio, page, size);
        return ResponseEntity.ok(socioPuestoService.listarAsignaciones(dniSocio, nombreSocio, page, size));
    }

    @PatchMapping("/puesto/{codigoPuesto}/reasignar")
    public ResponseEntity<SocioPuestoResponseDTO> reasignarPuesto(
            @PathVariable String codigoPuesto,
            @RequestParam String codigoSocio
    ) {
        log.info("Solicitud para reasignar puesto {} al socio {}", codigoPuesto, codigoSocio);
        return ResponseEntity.ok(socioPuestoService.reasignarPuesto(codigoPuesto, codigoSocio));
    }

}
