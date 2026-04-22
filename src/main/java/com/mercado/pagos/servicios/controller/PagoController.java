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

import java.time.LocalDate;
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

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener pago con id {}", id);
        return ResponseEntity.ok(pagoService.obtenerPagoPorId(id));
    }

    @GetMapping("/puesto/{codigoPuesto}")
    public ResponseEntity<List<PagoResponseDTO>> listarPagosPorCodigoPuesto(@PathVariable String codigoPuesto) {
        log.info("Solicitud para listar pagos del puesto {}", codigoPuesto);
        return ResponseEntity.ok(pagoService.listarPagosPorCodigoPuesto(codigoPuesto));
    }

    @GetMapping("/deuda/{idDeuda}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorDeuda(@PathVariable Long idDeuda) {
        log.info("Solicitud para obtener pago de deuda {}", idDeuda);
        return ResponseEntity.ok(pagoService.obtenerPagoPorDeuda(idDeuda));
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<PagoResponseDTO>> listarPagosPorFechas(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fin
    ) {
        log.info("Solicitud para listar pagos entre {} y {}", inicio, fin);
        return ResponseEntity.ok(pagoService.listarPagosPorFechas(inicio.atStartOfDay(), fin.atTime(23, 59, 59)));
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
