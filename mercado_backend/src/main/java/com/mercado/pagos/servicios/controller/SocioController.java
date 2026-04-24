package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    public ResponseEntity<PageResponseDTO<SocioResponseDTO>> listarSocios(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para listar socios en pagina {} con tamano {}", page, size);
        return ResponseEntity.ok(socioService.listarSocios(page, size));
    }

    @GetMapping("/buscar")
    public ResponseEntity<PageResponseDTO<SocioResponseDTO>> buscarSocios(
            @RequestParam String termino,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para buscar socios por termino {} en pagina {} con tamano {}", termino, page, size);
        return ResponseEntity.ok(socioService.buscarSocios(termino, page, size));
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

    @PatchMapping("/{id}/estado")
    public ResponseEntity<SocioResponseDTO> cambiarEstadoSocio(
            @PathVariable Long id,
            @RequestParam String estado
    ) {
        log.info("Solicitud para cambiar estado del socio con id {} a {}", id, estado);
        return ResponseEntity.ok(socioService.cambiarEstadoSocio(id, estado));
    }

}
