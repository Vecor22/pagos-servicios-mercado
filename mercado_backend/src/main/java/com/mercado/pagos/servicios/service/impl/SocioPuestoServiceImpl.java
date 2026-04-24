package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import com.mercado.pagos.servicios.repository.PuestoRepository;
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.repository.SocioRepository;
import com.mercado.pagos.servicios.service.SocioPuestoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocioPuestoServiceImpl implements SocioPuestoService {

    private final SocioPuestoRepository socioPuestoRepository;
    private final SocioRepository socioRepository;
    private final PuestoRepository puestoRepository;

    @Override
    public SocioPuestoResponseDTO asignarPuesto(AsignacionPuestoRequestDTO requestDTO) {
        Socio socio = buscarSocioActivoPorCodigo(requestDTO.getCodigoSocio());
        Puesto puesto = buscarPuestoPorCodigo(requestDTO.getCodigoPuesto());
        if (socioPuestoRepository.findByPuestoCodigoPuesto(requestDTO.getCodigoPuesto()).isPresent()) {
            throw new BusinessRuleException("El puesto ya tiene un socio asignado. Use la reasignacion de puesto");
        }

        SocioPuesto asignacion = SocioPuesto.builder()
                .socio(socio)
                .puesto(puesto)
                .fechaAsignacion(LocalDate.now())
                .build();

        SocioPuesto guardada = socioPuestoRepository.save(asignacion);
        log.info("Puesto {} asignado al socio {}", puesto.getCodigoPuesto(), socio.getCodigoSocio());
        return toResponse(guardada);
    }

    @Override
    public SocioPuestoResponseDTO reasignarPuesto(String codigoPuesto, String codigoSocio) {
        Socio nuevoSocio = buscarSocioActivoPorCodigo(codigoSocio);
        buscarPuestoPorCodigo(codigoPuesto);
        SocioPuesto asignacion = socioPuestoRepository.findByPuestoCodigoPuesto(codigoPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada para el puesto codigo: " + codigoPuesto));

        if (asignacion.getSocio().getCodigoSocio().equalsIgnoreCase(codigoSocio)) {
            throw new BusinessRuleException("El puesto ya se encuentra asignado al socio indicado");
        }

        asignacion.setSocio(nuevoSocio);
        asignacion.setFechaAsignacion(LocalDate.now());

        SocioPuesto guardada = socioPuestoRepository.save(asignacion);
        log.info("Puesto {} reasignado al socio {}", codigoPuesto, codigoSocio);
        return toResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDTO<SocioPuestoResponseDTO> listarAsignaciones(String dniSocio, String nombreSocio, int page, int size) {
        boolean hasDniFilter = dniSocio != null && !dniSocio.isBlank();
        boolean hasNameFilter = nombreSocio != null && !nombreSocio.isBlank();

        if (!hasDniFilter && !hasNameFilter) {
            Page<SocioPuesto> asignaciones = socioPuestoRepository.findAllBy(buildPageable(page, size));
            return PageResponseDTO.<SocioPuestoResponseDTO>builder()
                    .content(asignaciones.getContent().stream().map(this::toResponse).toList())
                    .page(asignaciones.getNumber())
                    .size(asignaciones.getSize())
                    .totalElements(asignaciones.getTotalElements())
                    .totalPages(asignaciones.getTotalPages())
                    .last(asignaciones.isLast())
                    .build();
        }

        String dniFilter = hasDniFilter ? dniSocio.trim() : null;
        String nameFilter = hasNameFilter ? normalize(nombreSocio) : null;

        List<SocioPuestoResponseDTO> filtradas = socioPuestoRepository.findAll().stream()
                .filter(asignacion -> !hasDniFilter || asignacion.getSocio().getDni().contains(dniFilter))
                .filter(asignacion -> !hasNameFilter || normalize(asignacion.getSocio().getNombres() + " " + asignacion.getSocio().getApellidos()).contains(nameFilter))
                .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                .map(this::toResponse)
                .toList();

        return slicePage(filtradas, page, size);
    }

    @Override
    public List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio) {
        return socioPuestoRepository.findBySocioId(idSocio).stream()
                .map(this::toResponse)
                .toList();
    }

    private Socio buscarSocioActivoPorCodigo(String codigoSocio) {
        Socio socio = socioRepository.findByCodigoSocio(codigoSocio)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con codigo: " + codigoSocio));
        if (socio.getEstado() != EstadoSocio.ACTIVO) {
            throw new BusinessRuleException("Solo se pueden asignar puestos a socios activos");
        }
        return socio;
    }

    private Puesto buscarPuestoPorCodigo(String codigoPuesto) {
        return puestoRepository.findByCodigoPuesto(codigoPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con codigo: " + codigoPuesto));
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    private SocioPuestoResponseDTO toResponse(SocioPuesto socioPuesto) {
        return SocioPuestoResponseDTO.builder()
                .id(socioPuesto.getId())
                .codigoSocio(socioPuesto.getSocio().getCodigoSocio())
                .dniSocio(socioPuesto.getSocio().getDni())
                .nombreCompletoSocio(socioPuesto.getSocio().getNombres() + " " + socioPuesto.getSocio().getApellidos())
                .codigoPuesto(socioPuesto.getPuesto().getCodigoPuesto())
                .fechaAsignacion(socioPuesto.getFechaAsignacion())
                .build();
    }

    private PageResponseDTO<SocioPuestoResponseDTO> slicePage(List<SocioPuestoResponseDTO> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);

        return PageResponseDTO.<SocioPuestoResponseDTO>builder()
                .content(items.subList(fromIndex, toIndex))
                .page(safePage)
                .size(safeSize)
                .totalElements(items.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

}
