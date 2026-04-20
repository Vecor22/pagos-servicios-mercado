package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.repository.PuestoRepository;
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
    private final ObjectMapper objectMapper;

    @Override
    public PuestoResponseDTO crearPuesto() {
        Puesto puesto = new Puesto();
        puesto.setCodigoPuesto(generarCodigoPuesto());

        Puesto guardado = puestoRepository.save(puesto);
        log.info("Puesto creado con id {} y codigo {}", guardado.getId(), guardado.getCodigoPuesto());
        return objectMapper.convertValue(guardado, PuestoResponseDTO.class);
    }

    @Override
    public List<PuestoResponseDTO> listarPuestos() {
        return puestoRepository.findAll().stream()
                .map(puesto -> objectMapper.convertValue(puesto, PuestoResponseDTO.class))
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

}
