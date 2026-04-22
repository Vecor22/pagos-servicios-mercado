package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PagoResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Comprobante;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.EstadoComprobante;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import com.mercado.pagos.servicios.repository.ComprobanteRepository;
import com.mercado.pagos.servicios.repository.DeudaRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.repository.PuestoRepository;
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
    private static final String COMPROBANTE_PREFIX = "COMP-";
    private static final String TIPO_COMPROBANTE = "RECIBO";

    private final PagoRepository pagoRepository;
    private final DeudaRepository deudaRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PuestoRepository puestoRepository;
    private final UsuarioRepository usuarioRepository;

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

        Pago pago = Pago.builder()
                .codigoPago(generarCodigoPago())
                .deuda(deuda)
                .montoPagado(requestDTO.getMontoPagado())
                .medioPago(requestDTO.getMedioPago())
                .numeroOperacion(requestDTO.getNumeroOperacion())
                .fechaPago(LocalDateTime.now())
                .estado(EstadoPago.REGISTRADO)
                .registradoPor(obtenerUsuarioSistema())
                .build();

        deuda.setEstado(EstadoDeuda.PAGADA);
        deudaRepository.save(deuda);

        Pago guardado = pagoRepository.save(pago);
        crearComprobanteParaPago(guardado);
        log.info("Pago registrado con id {} para deuda {}", guardado.getId(), deuda.getId());
        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponseDTO obtenerPagoPorDeuda(Long idDeuda) {
        deudaRepository.findById(idDeuda)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada con id: " + idDeuda));

        return pagoRepository.findByDeudaId(idDeuda)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado para deuda id: " + idDeuda));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPagos() {
        return pagoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> listarPagosPorCodigoPuesto(String codigoPuesto) {
        puestoRepository.findByCodigoPuesto(codigoPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con codigo: " + codigoPuesto));

        return pagoRepository.findByDeudaPuestoCodigoPuesto(codigoPuesto).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
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
        anularComprobanteSiExiste(pago);

        Pago guardado = pagoRepository.save(pago);
        log.info("Pago anulado con id {}", guardado.getId());
        return toResponse(guardado);
    }

    private String generarCodigoPago() {
        long siguiente = pagoRepository.count() + 1;
        return CODIGO_PREFIX + String.format("%06d", siguiente);
    }

    private void crearComprobanteParaPago(Pago pago) {
        Comprobante comprobante = Comprobante.builder()
                .pago(pago)
                .numeroComprobante(generarNumeroComprobante())
                .tipoComprobante(TIPO_COMPROBANTE)
                .fechaEmision(LocalDateTime.now())
                .estado(EstadoComprobante.EMITIDO)
                .build();

        Comprobante guardado = comprobanteRepository.save(comprobante);
        log.info("Comprobante {} generado automaticamente para pago {}", guardado.getId(), pago.getId());
    }

    private String generarNumeroComprobante() {
        long siguiente = comprobanteRepository.count() + 1;
        String numero = COMPROBANTE_PREFIX + String.format("%06d", siguiente);
        while (comprobanteRepository.findByNumeroComprobante(numero).isPresent()) {
            siguiente++;
            numero = COMPROBANTE_PREFIX + String.format("%06d", siguiente);
        }
        return numero;
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No existe un usuario registrado para asociar la operacion"));
    }

    private void anularComprobanteSiExiste(Pago pago) {
        comprobanteRepository.findByPagoId(pago.getId())
                .ifPresent(comprobante -> {
                    comprobante.setEstado(EstadoComprobante.ANULADO);
                    Comprobante guardado = comprobanteRepository.save(comprobante);
                    log.info("Comprobante {} anulado por anulacion del pago {}", guardado.getId(), pago.getId());
                });
    }

    private PagoResponseDTO toResponse(Pago pago) {
        return PagoResponseDTO.builder()
                .id(pago.getId())
                .codigoPago(pago.getCodigoPago())
                .idDeuda(pago.getDeuda().getId())
                .codigoDeuda(pago.getDeuda().getCodigoDeuda())
                .montoPagado(pago.getMontoPagado())
                .medioPago(pago.getMedioPago())
                .numeroOperacion(pago.getNumeroOperacion())
                .fechaPago(pago.getFechaPago())
                .estado(pago.getEstado().name())
                .build();
    }

}
