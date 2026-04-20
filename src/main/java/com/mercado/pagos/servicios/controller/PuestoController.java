package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;
import com.mercado.pagos.servicios.service.PuestoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/puestos")
public class PuestoController {

    private final PuestoService puestoService;

    @PostMapping
    public ResponseEntity<PuestoResponseDTO> crearPuesto() {
        log.info("Solicitud para crear puesto");
        return ResponseEntity.status(HttpStatus.CREATED).body(puestoService.crearPuesto());
    }

    @GetMapping
    public ResponseEntity<List<PuestoResponseDTO>> listarPuestos() {
        log.info("Solicitud para listar puestos");
        return ResponseEntity.ok(puestoService.listarPuestos());
    }

}
