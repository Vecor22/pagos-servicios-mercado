package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;
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
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.service.DeudaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeudaServiceImpl implements DeudaService {

    private static final String CODIGO_PREFIX = "DEUDA-";

    private final DeudaRepository deudaRepository;
    private final ConceptoCobroRepository conceptoCobroRepository;
    private final PuestoRepository puestoRepository;
    private final SocioPuestoRepository socioPuestoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public DeudaResponseDTO crearDeuda(DeudaRequestDTO requestDTO) {
        ConceptoCobro concepto = conceptoCobroRepository.findById(requestDTO.getIdConceptoCobro())
                .orElseThrow(() -> new ResourceNotFoundException("Concepto de cobro no encontrado con id: " + requestDTO.getIdConceptoCobro()));
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
        if (requestDTO.getIdPuesto() == null) {
            throw new BusinessRuleException("La deuda individual requiere un puesto asignado");
        }

        Puesto puesto = buscarPuesto(requestDTO.getIdPuesto());
        SocioPuesto asignacion = socioPuestoRepository.findByPuestoId(requestDTO.getIdPuesto())
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
        Puesto puestoOrigen = requestDTO.getIdPuesto() != null ? buscarPuesto(requestDTO.getIdPuesto()) : null;
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
    public List<DeudaResponseDTO> listarDeudas() {
        return deudaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<DeudaResponseDTO> listarDeudasPorPuesto(Long idPuesto) {
        return deudaRepository.findByPuestoId(idPuesto).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaResponseDTO> listarDeudasPorCodigoPuesto(String codigoPuesto, String estado) {
        buscarPuestoPorCodigo(codigoPuesto);

        List<Deuda> deudas = estado == null || estado.isBlank()
                ? deudaRepository.findByPuestoCodigoPuesto(codigoPuesto)
                : deudaRepository.findByPuestoCodigoPuestoAndEstado(codigoPuesto, parseEstadoDeuda(estado));

        return deudas.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeudaResponseDTO> listarDeudasPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return deudaRepository.findByFechaGeneracionBetween(inicio, fin).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DeudaResponseDTO exonerarDeuda(Long id, String motivo) {
        Deuda deuda = deudaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada con id: " + id));
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
