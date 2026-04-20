package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.SocioResponseDTO;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import com.mercado.pagos.servicios.repository.SocioRepository;
import com.mercado.pagos.servicios.service.SocioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocioServiceImpl implements SocioService {

    private static final String CODIGO_PREFIX = "SOCIO-";

    private final SocioRepository socioRepository;
    private final ObjectMapper objectMapper;

    @Override
    public SocioResponseDTO crearSocio(SocioRequestDTO requestDTO) {
        if (socioRepository.existsByDni(requestDTO.getDni())) {
            throw new IllegalArgumentException("Ya existe un socio con el DNI indicado");
        }

        Socio socio = objectMapper.convertValue(requestDTO, Socio.class);
        socio.setId(null);
        socio.setCodigoSocio(generarCodigoSocio());
        socio.setEstado(parseEstado(requestDTO.getEstado()));

        Socio guardado = socioRepository.save(socio);
        log.info("Socio creado con id {} y codigo {}", guardado.getId(), guardado.getCodigoSocio());
        return toResponse(guardado);
    }

    @Override
    public List<SocioResponseDTO> listarSocios() {
        return socioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public SocioResponseDTO obtenerSocioPorId(Long id) {
        return toResponse(buscarSocio(id));
    }

    @Override
    public SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO requestDTO) {
        Socio socio = buscarSocio(id);
        if (!socio.getDni().equals(requestDTO.getDni()) && socioRepository.existsByDni(requestDTO.getDni())) {
            throw new IllegalArgumentException("Ya existe un socio con el DNI indicado");
        }

        socio.setNombres(requestDTO.getNombres());
        socio.setApellidos(requestDTO.getApellidos());
        socio.setDni(requestDTO.getDni());
        socio.setTelefono(requestDTO.getTelefono());
        socio.setCorreo(requestDTO.getCorreo());
        socio.setEstado(parseEstado(requestDTO.getEstado()));

        Socio actualizado = socioRepository.save(socio);
        log.info("Socio actualizado con id {}", actualizado.getId());
        return toResponse(actualizado);
    }

    @Override
    public SocioResponseDTO desactivarSocio(Long id) {
        Socio socio = buscarSocio(id);
        socio.setEstado(EstadoSocio.INACTIVO);
        Socio actualizado = socioRepository.save(socio);
        log.info("Socio desactivado con id {}", actualizado.getId());
        return toResponse(actualizado);
    }

    private Socio buscarSocio(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Socio no encontrado con id: " + id));
    }

    private String generarCodigoSocio() {
        long siguiente = socioRepository.count() + 1;
        String codigo = CODIGO_PREFIX + String.format("%04d", siguiente);
        while (socioRepository.existsByCodigoSocio(codigo)) {
            siguiente++;
            codigo = CODIGO_PREFIX + String.format("%04d", siguiente);
        }
        return codigo;
    }

    private EstadoSocio parseEstado(String estado) {
        return EstadoSocio.valueOf(estado.toUpperCase());
    }

    private SocioResponseDTO toResponse(Socio socio) {
        return objectMapper.convertValue(socio, SocioResponseDTO.class);
    }

}
