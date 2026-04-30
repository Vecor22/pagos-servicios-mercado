package com.mercado.pagos.servicios.controller;

import com.mercado.pagos.servicios.dto.response.ResumenDeudasResponseDTO;
import com.mercado.pagos.servicios.dto.response.ResumenFlujoCajaResponseDTO;
import com.mercado.pagos.servicios.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/resumen-flujo-caja/dia")
    public ResponseEntity<ResumenFlujoCajaResponseDTO> obtenerResumenFlujoCajaDelDia(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fecha
    ) {
        log.info("Solicitud para resumen de flujo de caja del dia {}", fecha);
        return ResponseEntity.ok(reporteService.obtenerResumenFlujoCajaPorDia(fecha));
    }

    @GetMapping("/resumen-flujo-caja/mes")
    public ResponseEntity<ResumenFlujoCajaResponseDTO> obtenerResumenFlujoCajaDelMes(
            @RequestParam int anio,
            @RequestParam int mes
    ) {
        YearMonth periodo = YearMonth.of(anio, mes);
        log.info("Solicitud para resumen de flujo de caja del mes {}-{}", anio, mes);
        return ResponseEntity.ok(reporteService.obtenerResumenFlujoCajaPorMes(periodo));
    }

    @GetMapping("/resumen-flujo-caja/anio")
    public ResponseEntity<ResumenFlujoCajaResponseDTO> obtenerResumenFlujoCajaDelAnio(@RequestParam int anio) {
        log.info("Solicitud para resumen de flujo de caja del anio {}", anio);
        return ResponseEntity.ok(reporteService.obtenerResumenFlujoCajaPorAnio(anio));
    }

    @GetMapping("/resumen-flujo-caja/fechas")
    public ResponseEntity<ResumenFlujoCajaResponseDTO> obtenerResumenFlujoCajaPorFechas(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fin
    ) {
        log.info("Solicitud para resumen de flujo de caja entre {} y {}", inicio, fin);
        return ResponseEntity.ok(reporteService.obtenerResumenFlujoCaja(inicio.atStartOfDay(), finDelDia(fin)));
    }

    @GetMapping("/resumen-deudas/fechas")
    public ResponseEntity<ResumenDeudasResponseDTO> obtenerResumenDeudasPorFechas(
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate inicio,
            @RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate fin
    ) {
        log.info("Solicitud para resumen de deudas entre {} y {}", inicio, fin);
        return ResponseEntity.ok(reporteService.obtenerResumenDeudas(inicio.atStartOfDay(), finDelDia(fin)));
    }

    private LocalDateTime finDelDia(LocalDate fecha) {
        return fecha.atTime(23, 59, 59);
    }
}
