package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.request.AsignacionPuestoRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioPuestoResponseDTO;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.entity.SocioPuesto;
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
    private final ObjectMapper objectMapper;

    @Override
    public SocioPuestoResponseDTO asignarPuesto(AsignacionPuestoRequestDTO requestDTO) {
        Socio socio = socioRepository.findById(requestDTO.getIdSocio())
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con id: " + requestDTO.getIdSocio()));
        Puesto puesto = puestoRepository.findById(requestDTO.getIdPuesto())
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con id: " + requestDTO.getIdPuesto()));

        SocioPuesto asignacion = socioPuestoRepository.findByPuestoId(requestDTO.getIdPuesto())
                .orElseGet(SocioPuesto::new);
        asignacion.setSocio(socio);
        asignacion.setPuesto(puesto);
        asignacion.setFechaAsignacion(LocalDate.now());

        SocioPuesto guardada = socioPuestoRepository.save(asignacion);
        log.info("Puesto {} asignado al socio {}", puesto.getId(), socio.getId());
        return toResponse(guardada);
    }

    @Override
    public List<SocioPuestoResponseDTO> obtenerPuestosPorSocio(Long idSocio) {
        return socioPuestoRepository.findBySocioId(idSocio).stream()
                .map(this::toResponse)
                .toList();
    }

    private SocioPuestoResponseDTO toResponse(SocioPuesto socioPuesto) {
        SocioPuestoResponseDTO response = objectMapper.convertValue(socioPuesto, SocioPuestoResponseDTO.class);
        response.setIdSocio(socioPuesto.getSocio().getId());
        response.setNombreCompletoSocio(socioPuesto.getSocio().getNombres() + " " + socioPuesto.getSocio().getApellidos());
        response.setIdPuesto(socioPuesto.getPuesto().getId());
        response.setCodigoPuesto(socioPuesto.getPuesto().getCodigoPuesto());
        return response;
    }

}
