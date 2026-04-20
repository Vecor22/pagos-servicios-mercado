package com.mercado.pagos.servicios.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercado.pagos.servicios.dto.request.DeudaRequestDTO;
import com.mercado.pagos.servicios.dto.response.DeudaResponseDTO;
import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
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
    private final ObjectMapper objectMapper;

    @Override
    public DeudaResponseDTO crearDeuda(DeudaRequestDTO requestDTO) {
        ConceptoCobro concepto = conceptoCobroRepository.findById(requestDTO.getIdConceptoCobro())
                .orElseThrow(() -> new IllegalArgumentException("Concepto de cobro no encontrado con id: " + requestDTO.getIdConceptoCobro()));
        Puesto puesto = puestoRepository.findById(requestDTO.getIdPuesto())
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado con id: " + requestDTO.getIdPuesto()));
        SocioPuesto asignacion = socioPuestoRepository.findByPuestoId(requestDTO.getIdPuesto())
                .orElseThrow(() -> new IllegalArgumentException("El puesto no tiene un socio asignado"));

        Deuda deuda = objectMapper.convertValue(requestDTO, Deuda.class);
        deuda.setId(null);
        deuda.setCodigoDeuda(generarCodigoDeuda());
        deuda.setConceptoCobro(concepto);
        deuda.setPuesto(puesto);
        deuda.setSocio(asignacion.getSocio());
        deuda.setTipoGeneracion(TipoGeneracionDeuda.valueOf(requestDTO.getTipoGeneracion().toUpperCase()));
        deuda.setEstado(EstadoDeuda.PENDIENTE);
        deuda.setFechaGeneracion(LocalDateTime.now());
        deuda.setCreadoPor(obtenerUsuarioSistema());

        Deuda guardada = deudaRepository.save(deuda);
        log.info("Deuda creada con id {} para puesto {}", guardada.getId(), puesto.getId());
        return toResponse(guardada);
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
    public List<DeudaResponseDTO> listarDeudasPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return deudaRepository.findByFechaGeneracionBetween(inicio, fin).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DeudaResponseDTO exonerarDeuda(Long id, String motivo) {
        Deuda deuda = deudaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deuda no encontrada con id: " + id));
        if (deuda.getEstado() == EstadoDeuda.PAGADA) {
            throw new IllegalStateException("No se puede exonerar una deuda pagada");
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
        return CODIGO_PREFIX + String.format("%06d", siguiente);
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe un usuario registrado para asociar la operacion"));
    }

    private DeudaResponseDTO toResponse(Deuda deuda) {
        DeudaResponseDTO response = objectMapper.convertValue(deuda, DeudaResponseDTO.class);
        response.setIdConceptoCobro(deuda.getConceptoCobro().getId());
        response.setNombreConcepto(deuda.getConceptoCobro().getNombre());
        response.setIdPuesto(deuda.getPuesto().getId());
        response.setCodigoPuesto(deuda.getPuesto().getCodigoPuesto());
        response.setIdSocio(deuda.getSocio().getId());
        response.setNombreCompletoSocio(deuda.getSocio().getNombres() + " " + deuda.getSocio().getApellidos());
        return response;
    }

}
