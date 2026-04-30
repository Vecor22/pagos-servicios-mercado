package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.response.ComprobanteResponseDTO;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Comprobante;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.enums.EstadoComprobante;
import com.mercado.pagos.servicios.repository.ComprobanteRepository;
import com.mercado.pagos.servicios.service.ComprobanteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponseDTO obtenerComprobantePorPago(Long idPago) {
        return comprobanteRepository.findByPagoId(idPago)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Comprobante no encontrado para pago id: " + idPago));
    }

    private ComprobanteResponseDTO toResponse(Comprobante comprobante) {
        return ComprobanteResponseDTO.builder()
                .id(comprobante.getId())
                .idPago(comprobante.getPago().getId())
                .codigoPuesto(obtenerCodigoPuesto(comprobante))
                .dniSocio(obtenerDniSocio(comprobante))
                .nombreCompletoSocio(obtenerNombreCompletoSocio(comprobante))
                .numeroComprobante(comprobante.getNumeroComprobante())
                .tipoComprobante(comprobante.getTipoComprobante())
                .fechaEmision(comprobante.getFechaEmision())
                .estado(obtenerEstadoComprobante(comprobante).name())
                .build();
    }

    private String obtenerCodigoPuesto(Comprobante comprobante) {
        Pago pago = comprobante.getPago();
        if (pago == null || pago.getDeuda() == null || pago.getDeuda().getPuesto() == null) {
            return null;
        }
        return pago.getDeuda().getPuesto().getCodigoPuesto();
    }

    private String obtenerDniSocio(Comprobante comprobante) {
        Socio socio = obtenerSocio(comprobante);
        return socio != null ? socio.getDni() : null;
    }

    private String obtenerNombreCompletoSocio(Comprobante comprobante) {
        Socio socio = obtenerSocio(comprobante);
        return socio != null ? socio.getNombres() + " " + socio.getApellidos() : null;
    }

    private Socio obtenerSocio(Comprobante comprobante) {
        Pago pago = comprobante.getPago();
        if (pago == null || pago.getDeuda() == null) {
            return null;
        }
        return pago.getDeuda().getSocio();
    }

    private EstadoComprobante obtenerEstadoComprobante(Comprobante comprobante) {
        return comprobante.getEstado() != null ? comprobante.getEstado() : EstadoComprobante.EMITIDO;
    }

}
