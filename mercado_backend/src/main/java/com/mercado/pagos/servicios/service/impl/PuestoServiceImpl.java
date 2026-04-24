package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.dto.response.PageResponseDTO;
import com.mercado.pagos.servicios.dto.response.PuestoResponseDTO;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.repository.PuestoRepository;
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.service.PuestoService;
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
    public PageResponseDTO<PuestoResponseDTO> listarPuestos(boolean sinAsignacion, int page, int size) {
        if (sinAsignacion) {
            var filtrados = puestoRepository.findAll().stream()
                    .filter(puesto -> socioPuestoRepository.findByPuestoId(puesto.getId()).isEmpty())
                    .sorted((a, b) -> Long.compare(a.getId(), b.getId()))
                    .map(this::toResponse)
                    .toList();
            return slicePage(filtrados, page, size);
        }

        Page<Puesto> puestos = puestoRepository.findAll(buildPageable(page, size));
        return PageResponseDTO.<PuestoResponseDTO>builder()
                .content(puestos.getContent().stream().map(this::toResponse).toList())
                .page(puestos.getNumber())
                .size(puestos.getSize())
                .totalElements(puestos.getTotalElements())
                .totalPages(puestos.getTotalPages())
                .last(puestos.isLast())
                .build();
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

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        return PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "id"));
    }

    private PageResponseDTO<PuestoResponseDTO> slicePage(java.util.List<PuestoResponseDTO> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 10 : Math.min(size, 100);
        int fromIndex = Math.min(safePage * safeSize, items.size());
        int toIndex = Math.min(fromIndex + safeSize, items.size());
        int totalPages = items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize);

        return PageResponseDTO.<PuestoResponseDTO>builder()
                .content(items.subList(fromIndex, toIndex))
                .page(safePage)
                .size(safeSize)
                .totalElements(items.size())
                .totalPages(totalPages)
                .last(totalPages == 0 || safePage >= totalPages - 1)
                .build();
    }

    private PuestoResponseDTO toResponse(Puesto puesto) {
        return PuestoResponseDTO.builder()
                .id(puesto.getId())
                .codigoPuesto(puesto.getCodigoPuesto())
                .build();
    }

}
