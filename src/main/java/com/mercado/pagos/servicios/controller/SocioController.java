package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioResponseDTO;
import com.mercado.pagos.servicios.service.SocioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/socios")
public class SocioController {

    private final SocioService socioService;

    @PostMapping
    public ResponseEntity<SocioResponseDTO> crearSocio(@Valid @RequestBody SocioRequestDTO requestDTO) {
        log.info("Solicitud para crear socio con DNI {}", requestDTO.getDni());
        return ResponseEntity.status(HttpStatus.CREATED).body(socioService.crearSocio(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<SocioResponseDTO>> listarSocios() {
        log.info("Solicitud para listar socios");
        return ResponseEntity.ok(socioService.listarSocios());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> obtenerSocioPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener socio con id {}", id);
        return ResponseEntity.ok(socioService.obtenerSocioPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SocioResponseDTO> actualizarSocio(
            @PathVariable Long id,
            @Valid @RequestBody SocioRequestDTO requestDTO
    ) {
        log.info("Solicitud para actualizar socio con id {}", id);
        return ResponseEntity.ok(socioService.actualizarSocio(id, requestDTO));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<SocioResponseDTO> desactivarSocio(@PathVariable Long id) {
        log.info("Solicitud para desactivar socio con id {}", id);
        return ResponseEntity.ok(socioService.desactivarSocio(id));
    }

}
