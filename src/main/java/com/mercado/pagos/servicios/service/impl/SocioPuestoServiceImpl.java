package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

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
        Socio socio = buscarSocioActivo(requestDTO.getIdSocio());
        Puesto puesto = buscarPuesto(requestDTO.getIdPuesto());
        if (socioPuestoRepository.existsByPuestoId(requestDTO.getIdPuesto())) {
            throw new BusinessRuleException("El puesto ya tiene un socio asignado. Use la reasignacion de puesto");
        }

        SocioPuesto asignacion = SocioPuesto.builder()
                .socio(socio)
                .puesto(puesto)
                .fechaAsignacion(LocalDate.now())
                .build();

        SocioPuesto guardada = socioPuestoRepository.save(asignacion);
        log.info("Puesto {} asignado al socio {}", puesto.getId(), socio.getId());
        return toResponse(guardada);
    }

    @Override
    public SocioPuestoResponseDTO reasignarPuesto(Long idPuesto, Long idSocio) {
        Socio nuevoSocio = buscarSocioActivo(idSocio);
        buscarPuesto(idPuesto);
        SocioPuesto asignacion = socioPuestoRepository.findByPuestoId(idPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Asignacion no encontrada para el puesto id: " + idPuesto));

        if (asignacion.getSocio().getId().equals(idSocio)) {
            throw new BusinessRuleException("El puesto ya se encuentra asignado al socio indicado");
        }

        asignacion.setSocio(nuevoSocio);
        asignacion.setFechaAsignacion(LocalDate.now());

        SocioPuesto guardada = socioPuestoRepository.save(asignacion);
        log.info("Puesto {} reasignado al socio {}", idPuesto, idSocio);
        return toResponse(guardada);
    }

    @Override
    public List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio) {
        return socioPuestoRepository.findBySocioId(idSocio).stream()
                .map(this::toResponse)
                .toList();
    }

    private Socio buscarSocioActivo(Long idSocio) {
        Socio socio = socioRepository.findById(idSocio)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con id: " + idSocio));
        if (socio.getEstado() != EstadoSocio.ACTIVO) {
            throw new BusinessRuleException("Solo se pueden asignar puestos a socios activos");
        }
        return socio;
    }

    private Puesto buscarPuesto(Long idPuesto) {
        return puestoRepository.findById(idPuesto)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con id: " + idPuesto));
    }

    private SocioPuestoResponseDTO toResponse(SocioPuesto socioPuesto) {
        return SocioPuestoResponseDTO.builder()
                .id(socioPuesto.getId())
                .idSocio(socioPuesto.getSocio().getId())
                .nombreCompletoSocio(socioPuesto.getSocio().getNombres() + " " + socioPuesto.getSocio().getApellidos())
                .idPuesto(socioPuesto.getPuesto().getId())
                .codigoPuesto(socioPuesto.getPuesto().getCodigoPuesto())
                .fechaAsignacion(socioPuesto.getFechaAsignacion())
                .build();
    }

}
