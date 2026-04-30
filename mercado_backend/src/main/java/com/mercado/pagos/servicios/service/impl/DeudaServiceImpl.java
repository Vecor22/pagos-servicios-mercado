package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import com.mercado.pagos.servicios.model.enums.TipoGeneracionDeuda;
import com.mercado.pagos.servicios.repository.ConceptoCobroRepository;
import com.mercado.pagos.servicios.repository.DeudaRepository;
import com.mercado.pagos.servicios.repository.PuestoRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.service.DeudaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeudaServiceImpl implements DeudaService {

    private static final String CODIGO_PREFIX = "DEUDA-";

    private final DeudaRepository deudaRepository;
    private final ConceptoCobroRepository conceptoCobroRepository;
    private final PuestoRepository puestoRepository;
    private final PagoRepository pagoRepository;
    private final SocioPuestoRepository socioPuestoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public DeudaResponseDTO crearDeuda(DeudaRequestDTO requestDTO) {
        ConceptoCobro concepto = conceptoCobroRepository.findByNombre(requestDTO.getNombreConceptoCobro())
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado con nombre: " + requestDTO.getNombreConceptoCobro()));
        TipoGeneracionDeuda tipoGeneracion = parseTipoGeneracion(requestDTO.getTipoGeneracion());
        Usuario usuarioSistema = obtenerUsuarioSistema();

        if (tipoGeneracion == TipoGeneracionDeuda.REPARTIBLE) {
            return crearDeudaRepartible(requestDTO, concepto, usuarioSistema);
        }

        return crearDeudaIndividual(requestDTO, concepto, usuarioSistema);
    }

    private DeudaResponseDTO crearDeudaIndividual(
            DeudaRequestDTO requestDTO,
            ConceptoCobro concepto,
            Usuario usuarioSistema
    ) {
        if (requestDTO.getCodigoPuesto() == null || requestDTO.getCodigoPuesto().isBlank()) {
            throw new BusinessRuleException("La deuda individual requiere un puesto asignado");
        }

        Puesto puesto = buscarPuestoPorCodigo(requestDTO.getCodigoPuesto());
        SocioPuesto asignacion = socioPuestoRepository.findByPuestoCodigoPuesto(requestDTO.getCodigoPuesto())
                .orElseThrow(() -> new BusinessRuleException("El puesto no tiene un socio activo asignado. Genere una deuda repartible"));
        if (asignacion.getSocio().getEstado() != EstadoSocio.ACTIVO) {
            throw new BusinessRuleException("El socio asignado al puesto se encuentra inactivo. Genere una deuda repartible");
        }

        Socio socio = asignacion.getSocio();
        Deuda deuda = Deuda.builder()
                .codigoDeuda(generarCodigoDeuda())
                .conceptoCobro(concepto)
                .puesto(puesto)
                .socio(socio)
                .monto(requestDTO.getMonto())
                .tipoGeneracion(TipoGeneracionDeuda.INDIVIDUAL)
                .estado(EstadoDeuda.PENDIENTE)
                .fechaGeneracion(LocalDateTime.now())
                .observacion(requestDTO.getObservacion())
                .creadoPor(usuarioSistema)
                .build();

        Deuda guardada = deudaRepository.save(deuda);
        log.info("Deuda creada con id {} para puesto {}", guardada.getId(), puesto.getId());
        return toResponse(guardada);
    }

    private DeudaResponseDTO crearDeudaRepartible(
            DeudaRequestDTO requestDTO,
            ConceptoCobro concepto,
            Usuario usuarioSistema
    ) {
        Puesto puestoOrigen = requestDTO.getCodigoPuesto() != null && !requestDTO.getCodigoPuesto().isBlank()
                ? buscarPuestoPorCodigo(requestDTO.getCodigoPuesto())
                : null;
        List<SocioPuesto> participantes = obtenerAsignacionesConSocioActivo();
        validarReparto(requestDTO.getMonto(), participantes.size());

        Deuda deudaOrigen = Deuda.builder()
                .codigoDeuda(generarCodigoDeuda())
                .conceptoCobro(concepto)
                .puesto(puestoOrigen)
                .socio(null)
                .monto(requestDTO.getMonto())
                .tipoGeneracion(TipoGeneracionDeuda.REPARTIBLE)
                .estado(EstadoDeuda.DISTRIBUIDA)
                .fechaGeneracion(LocalDateTime.now())
                .observacion(requestDTO.getObservacion())
                .creadoPor(usuarioSistema)
                .build();

        Deuda origenGuardada = deudaRepository.save(deudaOrigen);
        List<BigDecimal> montos = calcularMontosRepartidos(requestDTO.getMonto(), participantes.size());

        for (int i = 0; i < participantes.size(); i++) {
            SocioPuesto participante = participantes.get(i);
            Deuda deudaHija = Deuda.builder()
                    .codigoDeuda(generarCodigoDeuda())
                    .conceptoCobro(concepto)
                    .puesto(participante.getPuesto())
                    .socio(participante.getSocio())
                    .deudaOrigen(origenGuardada)
                    .monto(montos.get(i))
                    .tipoGeneracion(TipoGeneracionDeuda.INDIVIDUAL)
                    .estado(EstadoDeuda.PENDIENTE)
                    .fechaGeneracion(LocalDateTime.now())
                    .observacion("Parte distribuida desde deuda " + origenGuardada.getCodigoDeuda())
                    .creadoPor(usuarioSistema)
                    .build();

            deudaRepository.save(deudaHija);
        }

        log.info("Deuda repartible creada con id {} y distribuida en {} deudas", origenGuardada.getId(), participantes.size());
        return toResponse(origenGuardada);
    }

    @Override
    public PageResponseDTO<DeudaResponseDTO> listarDeudas(int page, int size) {
        Page<Deuda> deudas = deudaRepository.findAllBy(buildPageable(page, size));
        return toPageResponse(deudas);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<DeudaResponseDTO> filtrarDeudas(
            String codigoPuesto,
            String estado,
            LocalDateTime inicio,
            LocalDateTime fin,
            int page,
            int size
    ) {
        Stream<Deuda> stream = deudaRepository.findAll().stream();

        if (codigoPuesto != null && !codigoPuesto.isBlank()) {
            stream = stream.filter(deuda -> deuda.getPuesto() != null
                    && codigoPuesto.equalsIgnoreCase(deuda.getPuesto().getCodigoPuesto()));
        }
        if (estado != null && !estado.isBlank()) {
            EstadoDeuda estadoDeuda = parseEstadoDeuda(estado);
            stream = stream.filter(deuda -> deuda.getEstado() == estadoDeuda);
        }
        if (inicio != null) {
            stream = stream.filter(deuda -> !deuda.getFechaGeneracion().isBefore(inicio));
        }
        if (fin != null) {
            stream = stream.filter(deuda -> !deuda.getFechaGeneracion().isAfter(fin));
        }

        List<DeudaResponseDTO> filtradas = stream
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(this::toResponse)
                .toList();

        return slicePage(filtradas, page, size);
    }

    @Override
    public DeudaResponseDTO exonerarDeuda(Long id, String motivo) {
        Deuda deuda = deudaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada con id: " + id));
        if (deuda.getEstado() == EstadoDeuda.PAGADA) {
            if (pagoRepository.findByDeudaId(id).isPresent()) {
                throw new BusinessRuleException("La deuda ha sido pagada y primero debe anularse el pago para luego poder exonerarla");
            }
            throw new BusinessRuleException("La deuda ha sido pagada y no puede exonerarse directamente");
        }
        if (deuda.getEstado() != EstadoDeuda.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden exonerar deudas pendientes");
        }

        deuda.setEstado(EstadoDeuda.EXONERADA);
        deuda.setExoneradoPor(obtenerUsuarioSistema());
        deuda.setFechaExoneracion(LocalDateTime.now());
        deuda.setMotivoExoneracion(motivo);

        Deuda guardada = deudaRepository.save(deuda);
        log.info("Deuda exonerada con id {}", guardada.getId());
        return toResponse(guardada);
    }

    private String generarCodigoDeuda() {
        long siguiente = deudaRepository.count() + 1;
        String codigo = CODIGO_PREFIX + String.format("%06d", siguiente);
        while (deudaRepository.existsByCodigoDeuda(codigo)) {
            siguiente++;
            codigo = CODIGO_PREFIX + String.format("%06d", siguiente);
        }
        return codigo;
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No existe un usuario registrado para asociar la operacion"));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    private PageResponseDTO<DeudaResponseDTO> toPageResponse(Page<Deuda> deudas) {
        return PageResponseDTO.<DeudaResponseDTO>builder()
                .content(deudas.getContent().stream().map(this::toResponse).toList())
                .page(deudas.getNumber())
                .size(deudas.getSize())
                .totalElements(deudas.getTotalElements())
                .totalPages(deudas.getTotalPages())
                .last(deudas.isLast())
                .build();
    }

    private PageResponseDTO<DeudaResponseDTO> slicePage(List<DeudaResponseDTO> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);

        return PageResponseDTO.<DeudaResponseDTO>builder()
                .content(items.subList(fromIndex, toIndex))
                .page(safePage)
                .size(safeSize)
                .totalElements(items.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
    }

    private DeudaResponseDTO toResponse(Deuda deuda) {
        return DeudaResponseDTO.builder()
                .id(deuda.getId())
                .codigoDeuda(deuda.getCodigoDeuda())
                .idConceptoCobro(deuda.getConceptoCobro().getId())
                .nombreConcepto(deuda.getConceptoCobro().getNombre())
                .idPuesto(deuda.getPuesto() != null ? deuda.getPuesto().getId() : null)
                .codigoPuesto(deuda.getPuesto() != null ? deuda.getPuesto().getCodigoPuesto() : null)
                .idSocio(deuda.getSocio() != null ? deuda.getSocio().getId() : null)
                .nombreCompletoSocio(deuda.getSocio() != null ? deuda.getSocio().getNombres() + " " + deuda.getSocio().getApellidos() : null)
                .idDeudaOrigen(deuda.getDeudaOrigen() != null ? deuda.getDeudaOrigen().getId() : null)
                .codigoDeudaOrigen(deuda.getDeudaOrigen() != null ? deuda.getDeudaOrigen().getCodigoDeuda() : null)
                .monto(deuda.getMonto())
                .tipoGeneracion(deuda.getTipoGeneracion().name())
                .estado(deuda.getEstado().name())
                .fechaGeneracion(deuda.getFechaGeneracion())
                .observacion(deuda.getObservacion())
                .creadoPorUsername(deuda.getCreadoPor() != null ? deuda.getCreadoPor().getUsername() : null)
                .actualizadoPorUsername(deuda.getActualizadoPor() != null ? deuda.getActualizadoPor().getUsername() : null)
                .fechaActualizacion(deuda.getFechaActualizacion())
                .exoneradoPorUsername(deuda.getExoneradoPor() != null ? deuda.getExoneradoPor().getUsername() : null)
                .fechaExoneracion(deuda.getFechaExoneracion())
                .motivoExoneracion(deuda.getMotivoExoneracion())
                .build();
    }

    private Puesto buscarPuesto(Long idPuesto) {
        return puestoRepository.findById(idPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con id: " + idPuesto));
    }

    private Puesto buscarPuestoPorCodigo(String codigoPuesto) {
        return puestoRepository.findByCodigoPuesto(codigoPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con codigo: " + codigoPuesto));
    }

    private TipoGeneracionDeuda parseTipoGeneracion(String tipoGeneracion) {
        return TipoGeneracionDeuda.valueOf(tipoGeneracion.toUpperCase());
    }

    private EstadoDeuda parseEstadoDeuda(String estado) {
        try {
            return EstadoDeuda.valueOf(estado.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BusinessRuleException("Estado de deuda invalido: " + estado);
        }
    }

    private List<SocioPuesto> obtenerAsignacionesConSocioActivo() {
        return socioPuestoRepository.findAll().stream()
                .filter(asignacion -> asignacion.getSocio().getEstado() == EstadoSocio.ACTIVO)
                .toList();
    }

    private void validarReparto(BigDecimal monto, int cantidadParticipantes) {
        if (cantidadParticipantes == 0) {
            throw new BusinessRuleException("No existen puestos con socios activos para repartir la deuda");
        }

        BigDecimal montoMinimo = BigDecimal.valueOf(cantidadParticipantes).multiply(new BigDecimal("0.01"));
        if (monto.compareTo(montoMinimo) < 0) {
            throw new BusinessRuleException("El monto no permite repartir al menos 0.01 por participante");
        }
    }

    private List<BigDecimal> calcularMontosRepartidos(BigDecimal monto, int cantidadParticipantes) {
        BigDecimal base = monto.divide(BigDecimal.valueOf(cantidadParticipantes), 2, RoundingMode.DOWN);
        BigDecimal totalBase = base.multiply(BigDecimal.valueOf(cantidadParticipantes));
        BigDecimal diferencia = monto.subtract(totalBase);

        return java.util.stream.IntStream.range(0, cantidadParticipantes)
                .mapToObj(index -> index == cantidadParticipantes - 1 ? base.add(diferencia) : base)
                .toList();
    }

}
