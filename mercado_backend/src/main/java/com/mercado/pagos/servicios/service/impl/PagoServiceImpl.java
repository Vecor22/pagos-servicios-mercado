package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.PagoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
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
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.service.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final UsuarioRepository usuarioRepository;

    @Override
    public PagoResponseDTO registrarPago(PagoRequestDTO requestDTO) {
        Deuda deuda = deudaRepository.findByCodigoDeuda(requestDTO.getCodigoDeuda())
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada con codigo: " + requestDTO.getCodigoDeuda()));
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
    public PageResponseDTO<PagoResponseDTO> listarPagos(
            String codigoPago,
            String codigoDeuda,
            LocalDateTime inicio,
            LocalDateTime fin,
            int page,
            int size
    ) {
        if ((inicio == null) != (fin == null)) {
            throw new BusinessRuleException("Si se filtra por fechas, se deben ingresar ambas fechas");
        }

        if (codigoPago == null && codigoDeuda == null && inicio == null && fin == null) {
            Pageable pageable = PageRequest.of(page, size);
            var pagosPage = pagoRepository.findAll(pageable);
            return PageResponseDTO.<PagoResponseDTO>builder()
                    .content(pagosPage.getContent().stream().map(this::toResponse).toList())
                    .page(pagosPage.getNumber())
                    .size(pagosPage.getSize())
                    .totalElements(pagosPage.getTotalElements())
                    .totalPages(pagosPage.getTotalPages())
                    .last(pagosPage.isLast())
                    .build();
        }

        List<PagoResponseDTO> filtrados = pagoRepository.findAll().stream()
                .filter(pago -> matchesFilter(pago.getCodigoPago(), codigoPago))
                .filter(pago -> matchesFilter(pago.getDeuda().getCodigoDeuda(), codigoDeuda))
                .filter(pago -> matchesDateRange(pago.getFechaPago(), inicio, fin))
                .map(this::toResponse)
                .toList();

        int safeSize = Math.max(size, 1);
        int safePage = Math.max(page, 0);
        int fromIndex = safePage * safeSize;
        int toIndex = Math.min(fromIndex + safeSize, filtrados.size());
        List<PagoResponseDTO> content = fromIndex >= filtrados.size()
                ? List.of()
                : filtrados.subList(fromIndex, toIndex);

        int totalPages = filtrados.isEmpty() ? 0 : (int) Math.ceil((double) filtrados.size() / safeSize);

        return PageResponseDTO.<PagoResponseDTO>builder()
                .content(content)
                .page(safePage)
                .size(safeSize)
                .totalElements(filtrados.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
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

    private boolean matchesFilter(String actualValue, String filterValue) {
        if (filterValue == null || filterValue.isBlank()) {
            return true;
        }

        return actualValue != null && actualValue.toLowerCase().contains(filterValue.trim().toLowerCase());
    }

    private boolean matchesDateRange(LocalDateTime fechaPago, LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return true;
        }

        return (fechaPago.isEqual(inicio) || fechaPago.isAfter(inicio))
                && (fechaPago.isEqual(fin) || fechaPago.isBefore(fin));
    }

    private PagoResponseDTO toResponse(Pago pago) {
        return PagoResponseDTO.builder()
                .id(pago.getId())
                .codigoPago(pago.getCodigoPago())
                .idDeuda(pago.getDeuda().getId())
                .codigoDeuda(pago.getDeuda().getCodigoDeuda())
                .codigoPuesto(pago.getDeuda().getPuesto() != null ? pago.getDeuda().getPuesto().getCodigoPuesto() : null)
                .nombreCompletoSocio(pago.getDeuda().getSocio() != null
                        ? pago.getDeuda().getSocio().getNombres() + " " + pago.getDeuda().getSocio().getApellidos()
                        : null)
                .montoPagado(pago.getMontoPagado())
                .medioPago(pago.getMedioPago())
                .numeroOperacion(pago.getNumeroOperacion())
                .fechaPago(pago.getFechaPago())
                .estado(pago.getEstado().name())
                .registradoPorUsername(pago.getRegistradoPor() != null ? pago.getRegistradoPor().getUsername() : null)
                .anuladoPorUsername(pago.getAnuladoPor() != null ? pago.getAnuladoPor().getUsername() : null)
                .fechaAnulacion(pago.getFechaAnulacion())
                .motivoAnulacion(pago.getMotivoAnulacion())
                .build();
    }

}
