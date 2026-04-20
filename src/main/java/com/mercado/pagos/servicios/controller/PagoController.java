package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PagoResponseDTO;
import com.mercado.pagos.servicios.service.PagoService;
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
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrarPago(@Valid @RequestBody PagoRequestDTO requestDTO) {
        log.info("Solicitud para registrar pago de deuda {}", requestDTO.getIdDeuda());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listarPagos() {
        log.info("Solicitud para listar pagos");
        return ResponseEntity.ok(pagoService.listarPagos());
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<PagoResponseDTO>> listarPagosPorFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin
    ) {
        log.info("Solicitud para listar pagos entre {} y {}", inicio, fin);
        return ResponseEntity.ok(pagoService.listarPagosPorFechas(inicio, fin));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<PagoResponseDTO> anularPago(
            @PathVariable Long id,
            @RequestParam String motivo
    ) {
        log.info("Solicitud para anular pago con id {}", id);
        return ResponseEntity.ok(pagoService.anularPago(id, motivo));
    }

}
