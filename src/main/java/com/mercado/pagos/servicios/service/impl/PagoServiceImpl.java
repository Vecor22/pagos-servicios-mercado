package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PagoResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import com.mercado.pagos.servicios.repository.DeudaRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PagoServiceImpl implements PagoService {

    private static final String CODIGO_PREFIX = "PAGO-";

    private final PagoRepository pagoRepository;
    private final DeudaRepository deudaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Override
    public PagoResponseDTO registrarPago(PagoRequestDTO requestDTO) {
        Deuda deuda = deudaRepository.findById(requestDTO.getIdDeuda())
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada con id: " + requestDTO.getIdDeuda()));
        if (deuda.getEstado() != EstadoDeuda.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden pagar deudas pendientes");
        }
        if (requestDTO.getMontoPagado().compareTo(deuda.getMonto()) != 0) {
            throw new BusinessRuleException("El monto pagado debe ser igual al monto de la deuda");
        }

        Pago pago = objectMapper.convertValue(requestDTO, Pago.class);
        pago.setId(null);
        pago.setCodigoPago(generarCodigoPago());
        pago.setDeuda(deuda);
        pago.setFechaPago(LocalDateTime.now());
        pago.setEstado(EstadoPago.REGISTRADO);
        pago.setRegistradoPor(obtenerUsuarioSistema());

        deuda.setEstado(EstadoDeuda.PAGADA);
        deudaRepository.save(deuda);

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago registrado con id {} para deuda {}", guardado.getId(), deuda.getId());
        return toResponse(guardado);
    }

    @Override
    public List<PagoResponseDTO> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<PagoResponseDTO> listarPagosPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return pagoRepository.findByFechaPagoBetween(inicio, fin).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PagoResponseDTO anularPago(Long id, String motivo) {
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new BusinessRuleException("El pago ya se encuentra anulado");
        }

        pago.setEstado(EstadoPago.ANULADO);
        pago.setAnuladoPor(obtenerUsuarioSistema());
        pago.setFechaAnulacion(LocalDateTime.now());
        pago.setMotivoAnulacion(motivo);

        Deuda deuda = pago.getDeuda();
        deuda.setEstado(EstadoDeuda.PENDIENTE);
        deudaRepository.save(deuda);

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago anulado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    private String generarCodigoPago() {
        long siguiente = pagoRepository.count() + 1;
        return CODIGO_PREFIX + String.format("%06d", siguiente);
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No existe un usuario registrado para asociar la operacion"));
    }

    private PagoResponseDTO toResponse(Pago pago) {
        PagoResponseDTO response = objectMapper.convertValue(pago, PagoResponseDTO.class);
        response.setIdDeuda(pago.getDeuda().getId());
        response.setCodigoDeuda(pago.getDeuda().getCodigoDeuda());
        return response;
    }

}
