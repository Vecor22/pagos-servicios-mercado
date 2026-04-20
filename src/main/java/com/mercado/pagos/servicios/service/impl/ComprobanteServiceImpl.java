package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.response.ComprobanteResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.DuplicateResourceException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Comprobante;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import com.mercado.pagos.servicios.repository.ComprobanteRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private static final String CODIGO_PREFIX = "COMP-";
    private static final String TIPO_COMPROBANTE = "RECIBO";

    private final ComprobanteRepository comprobanteRepository;
    private final PagoRepository pagoRepository;
    private final ObjectMapper objectMapper;

    @Override
    public ComprobanteResponseDTO generarComprobante(Long idPago) {
        comprobanteRepository.findByPagoId(idPago)
                .ifPresent(comprobante -> {
                    throw new DuplicateResourceException("Ya existe un comprobante para el pago indicado");
                });

        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + idPago));
        if (pago.getEstado() != EstadoPago.REGISTRADO) {
            throw new BusinessRuleException("Solo se puede generar comprobante de un pago registrado");
        }

        Comprobante comprobante = new Comprobante();
        comprobante.setPago(pago);
        comprobante.setNumeroComprobante(generarNumeroComprobante());
        comprobante.setTipoComprobante(TIPO_COMPROBANTE);
        comprobante.setFechaEmision(LocalDateTime.now());

        Comprobante guardado = comprobanteRepository.save(comprobante);
        log.info("Comprobante generado con id {} para pago {}", guardado.getId(), idPago);
        return toResponse(guardado);
    }

    @Override
    public ComprobanteResponseDTO obtenerComprobantePorPago(Long idPago) {
        return comprobanteRepository.findByPagoId(idPago)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado para pago id: " + idPago));
    }

    private String generarNumeroComprobante() {
        long siguiente = comprobanteRepository.count() + 1;
        String numero = CODIGO_PREFIX + String.format("%06d", siguiente);
        while (comprobanteRepository.findByNumeroComprobante(numero).isPresent()) {
            siguiente++;
            numero = CODIGO_PREFIX + String.format("%06d", siguiente);
        }
        return numero;
    }

    private ComprobanteResponseDTO toResponse(Comprobante comprobante) {
        ComprobanteResponseDTO response = objectMapper.convertValue(comprobante, ComprobanteResponseDTO.class);
        response.setIdPago(comprobante.getPago().getId());
        return response;
    }

}
