package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;
import com.mercado.pagos.servicios.service.DeudaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/deudas")
public class DeudaController {

    private final DeudaService deudaService;

    @PostMapping
    public ResponseEntity<DeudaResponseDTO> crearDeuda(@Valid @RequestBody DeudaRequestDTO requestDTO) {
        log.info("Solicitud para crear deuda para puesto {}", requestDTO.getIdPuesto());
        return ResponseEntity.status(HttpStatus.CREATED).body(deudaService.crearDeuda(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<DeudaResponseDTO>> listarDeudas() {
        log.info("Solicitud para listar deudas");
        return ResponseEntity.ok(deudaService.listarDeudas());
    }

    @GetMapping("/puesto/{idPuesto}")
    public ResponseEntity<List<DeudaResponseDTO>> listarDeudasPorPuesto(@PathVariable Long idPuesto) {
        log.info("Solicitud para listar deudas del puesto {}", idPuesto);
        return ResponseEntity.ok(deudaService.listarDeudasPorPuesto(idPuesto));
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<DeudaResponseDTO>> listarDeudasPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        log.info("Solicitud para listar deudas entre {} y {}", inicio, fin);
        return ResponseEntity.ok(deudaService.listarDeudasPorFechas(inicio, fin));
    }

    @PatchMapping("/{id}/exonerar")
    public ResponseEntity<DeudaResponseDTO> exonerarDeuda(
            @PathVariable Long id,
            @RequestParam String motivo
    ) {
        log.info("Solicitud para exonerar deuda con id {}", id);
        return ResponseEntity.ok(deudaService.exonerarDeuda(id, motivo));
    }

}
