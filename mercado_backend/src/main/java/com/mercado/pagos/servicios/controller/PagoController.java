package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pagos")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrarPago(@Valid @RequestBody PagoRequestDTO requestDTO) {
        log.info("Solicitud para registrar pago de deuda {}", requestDTO.getCodigoDeuda());
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(requestDTO));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<PagoResponseDTO>> listarPagos(
            @RequestParam(required = false) String codigoPago,
            @RequestParam(required = false) String codigoDeuda,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Solicitud para listar pagos con filtros codigoPago={}, codigoDeuda={}, inicio={}, fin={}, page={}, size={}",
                codigoPago, codigoDeuda, inicio, fin, page, size);
        return ResponseEntity.ok(pagoService.listarPagos(
                codigoPago,
                codigoDeuda,
                inicio != null ? inicio.atStartOfDay() : null,
                fin != null ? fin.atTime(23, 59, 59) : null,
                page,
                size
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorId(@PathVariable Long id) {
        log.info("Solicitud para obtener pago con id {}", id);
        return ResponseEntity.ok(pagoService.obtenerPagoPorId(id));
    }

    @GetMapping("/deuda/{idDeuda}")
    public ResponseEntity<PagoResponseDTO> obtenerPagoPorDeuda(@PathVariable Long idDeuda) {
        log.info("Solicitud para obtener pago de deuda {}", idDeuda);
        return ResponseEntity.ok(pagoService.obtenerPagoPorDeuda(idDeuda));
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
