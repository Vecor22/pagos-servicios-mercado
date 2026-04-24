package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/deudas")
public class DeudaController {

    private final DeudaService deudaService;

    @PostMapping
    public ResponseEntity<DeudaResponseDTO> crearDeuda(@Valid @RequestBody DeudaRequestDTO requestDTO) {
        log.info("Solicitud para crear deuda para puesto {}", requestDTO.getCodigoPuesto());
        return ResponseEntity.status(HttpStatus.CREATED).body(deudaService.crearDeuda(requestDTO));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<DeudaResponseDTO>> listarDeudas(
            @RequestParam(required = false) String codigoPuesto,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        LocalDateTime fechaInicio = inicio != null ? inicio.atStartOfDay() : null;
        LocalDateTime fechaFin = fin != null ? fin.atTime(23, 59, 59) : null;
        log.info("Solicitud para listar/filtrar deudas. codigoPuesto={}, estado={}, inicio={}, fin={}, page={}, size={}",
                codigoPuesto, estado, inicio, fin, page, size);
        if ((codigoPuesto == null || codigoPuesto.isBlank()) && (estado == null || estado.isBlank()) && fechaInicio == null && fechaFin == null) {
            return ResponseEntity.ok(deudaService.listarDeudas(page, size));
        }
        return ResponseEntity.ok(deudaService.filtrarDeudas(codigoPuesto, estado, fechaInicio, fechaFin, page, size));
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
