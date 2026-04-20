package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.response.ComprobanteResponseDTO;
import com.mercado.pagos.servicios.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/comprobantes")
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @GetMapping("/pago/{idPago}")
    public ResponseEntity<ComprobanteResponseDTO> obtenerComprobantePorPago(@PathVariable Long idPago) {
        log.info("Solicitud para obtener comprobante del pago {}", idPago);
        return ResponseEntity.ok(comprobanteService.obtenerComprobantePorPago(idPago));
    }

    @PostMapping("/{idPago}")
    public ResponseEntity<ComprobanteResponseDTO> generarComprobante(@PathVariable Long idPago) {
        log.info("Solicitud para generar comprobante del pago {}", idPago);
        return ResponseEntity.status(HttpStatus.CREATED).body(comprobanteService.generarComprobante(idPago));
    }

}
