package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;
import com.mercado.pagos.servicios.service.PuestoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/codigo/{codigoPuesto}")
    public ResponseEntity<PuestoResponseDTO> obtenerPuestoPorCodigo(@PathVariable String codigoPuesto) {
        log.info("Solicitud para obtener puesto con codigo {}", codigoPuesto);
        return ResponseEntity.ok(puestoService.obtenerPuestoPorCodigo(codigoPuesto));
    }

    @GetMapping("/socio/dni/{dni}")
    public ResponseEntity<List<PuestoResponseDTO>> listarPuestosPorDniSocio(@PathVariable String dni) {
        log.info("Solicitud para listar puestos del socio con DNI {}", dni);
        return ResponseEntity.ok(puestoService.listarPuestosPorDniSocio(dni));
    }

    @GetMapping
    public ResponseEntity<List<PuestoResponseDTO>> listarPuestos() {
        log.info("Solicitud para listar puestos");
        return ResponseEntity.ok(puestoService.listarPuestos());
    }

}
