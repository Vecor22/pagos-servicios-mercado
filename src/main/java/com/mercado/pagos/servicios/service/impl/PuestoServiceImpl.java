package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;
import com.mercado.pagos.servicios.exception.ResourceNotFoundException;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.repository.PuestoRepository;
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.service.PuestoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PuestoServiceImpl implements PuestoService {

    private static final String CODIGO_PREFIX = "PUESTO-";

    private final PuestoRepository puestoRepository;
    private final SocioPuestoRepository socioPuestoRepository;

    @Override
    public PuestoResponseDTO crearPuesto() {
        Puesto puesto = Puesto.builder()
                .codigoPuesto(generarCodigoPuesto())
                .build();

        Puesto guardado = puestoRepository.save(puesto);
        log.info("Puesto creado con id {} y codigo {}", guardado.getId(), guardado.getCodigoPuesto());
        return toResponse(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public PuestoResponseDTO obtenerPuestoPorCodigo(String codigoPuesto) {
        return puestoRepository.findByCodigoPuesto(codigoPuesto)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Puesto no encontrado con codigo: " + codigoPuesto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuestoResponseDTO> listarPuestos() {
        return puestoRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PuestoResponseDTO> listarPuestosPorDniSocio(String dni) {
        return socioPuestoRepository.findBySocioDni(dni).stream()
                .map(asignacion -> toResponse(asignacion.getPuesto()))
                .toList();
    }

    private String generarCodigoPuesto() {
        long siguiente = puestoRepository.count() + 1;
        String codigo = CODIGO_PREFIX + String.format("%03d", siguiente);
        while (puestoRepository.existsByCodigoPuesto(codigo)) {
            siguiente++;
            codigo = CODIGO_PREFIX + String.format("%03d", siguiente);
        }
        return codigo;
    }

    private PuestoResponseDTO toResponse(Puesto puesto) {
        return PuestoResponseDTO.builder()
                .id(puesto.getId())
                .codigoPuesto(puesto.getCodigoPuesto())
                .build();
    }

}
