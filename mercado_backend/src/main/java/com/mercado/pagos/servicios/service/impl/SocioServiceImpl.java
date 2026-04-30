package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.request.SocioRequestDTO;
import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.SocioResponseDTO;
import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.exception.DuplicateResourceException;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import com.mercado.pagos.servicios.repository.SocioRepository;
import com.mercado.pagos.servicios.service.SocioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SocioServiceImpl implements SocioService {

    private static final String CODIGO_PREFIX = "SOCIO-";

    private final SocioRepository socioRepository;

    @Override
    public SocioResponseDTO crearSocio(SocioRequestDTO requestDTO) {
        if (socioRepository.existsByDni(requestDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe un socio con el DNI indicado");
        }

        Socio socio = Socio.builder()
                .codigoSocio(generarCodigoSocio())
                .nombres(requestDTO.getNombres())
                .apellidos(requestDTO.getApellidos())
                .dni(requestDTO.getDni())
                .telefono(requestDTO.getTelefono())
                .correo(requestDTO.getCorreo())
                .estado(parseEstado(requestDTO.getEstado()))
                .build();

        Socio guardado = socioRepository.save(socio);
        log.info("Socio creado con id {} y codigo {}", guardado.getId(), guardado.getCodigoSocio());
        return toResponse(guardado);
    }

    @Override
    public PageResponseDTO<SocioResponseDTO> listarSocios(int page, int size) {
        Page<Socio> socios = socioRepository.findAllBy(buildPageable(page, size));
        return toPageResponse(socios);
    }

    @Override
    public PageResponseDTO<SocioResponseDTO> buscarSocios(String termino, int page, int size) {
        Page<Socio> socios = socioRepository.findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(
                termino,
                termino,
                termino,
                buildPageable(page, size)
        );
        return toPageResponse(socios);
    }

    @Override
    public SocioResponseDTO obtenerSocioPorId(Long id) {
        return toResponse(buscarSocio(id));
    }

    @Override
    public SocioResponseDTO actualizarSocio(Long id, SocioRequestDTO requestDTO) {
        Socio socio = buscarSocio(id);
        if (!socio.getDni().equals(requestDTO.getDni()) && socioRepository.existsByDni(requestDTO.getDni())) {
            throw new DuplicateResourceException("Ya existe un socio con el DNI indicado");
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
    public SocioResponseDTO cambiarEstadoSocio(Long id, String estado) {
        Socio socio = buscarSocio(id);
        EstadoSocio nuevoEstado = parseEstado(estado);
        if (socio.getEstado() == nuevoEstado) {
            throw new BusinessRuleException("El socio ya se encuentra en estado " + nuevoEstado.name());
        }

        socio.setEstado(nuevoEstado);
        Socio actualizado = socioRepository.save(socio);
        log.info("Estado del socio con id {} cambiado a {}", actualizado.getId(), nuevoEstado);
        return toResponse(actualizado);
    }

    private Socio buscarSocio(Long id) {
        return socioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Socio no encontrado con id: " + id));
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

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    private PageResponseDTO<SocioResponseDTO> toPageResponse(Page<Socio> socios) {
        return PageResponseDTO.<SocioResponseDTO>builder()
                .content(socios.getContent().stream().map(this::toResponse).toList())
                .page(socios.getNumber())
                .size(socios.getSize())
                .totalElements(socios.getTotalElements())
                .totalPages(socios.getTotalPages())
                .last(socios.isLast())
                .build();
    }

    private SocioResponseDTO toResponse(Socio socio) {
        return SocioResponseDTO.builder()
                .id(socio.getId())
                .codigoSocio(socio.getCodigoSocio())
                .nombres(socio.getNombres())
                .apellidos(socio.getApellidos())
                .dni(socio.getDni())
                .telefono(socio.getTelefono())
                .correo(socio.getCorreo())
                .estado(socio.getEstado().name())
                .build();
    }

}
