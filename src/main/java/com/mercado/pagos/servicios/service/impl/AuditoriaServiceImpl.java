package com.mercado.pagos.servicios.service.impl;

import com.mercado.pagos.servicios.exception.BusinessRuleException;
import com.mercado.pagos.servicios.model.entity.Auditoria;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.repository.AuditoriaRepository;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import com.mercado.pagos.servicios.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void registrarAccion(String accion, String entidad, Long idEntidad, String descripcion) {
        Auditoria auditoria = Auditoria.builder()
                .usuario(obtenerUsuarioSistema())
                .accion(accion)
                .entidad(entidad)
                .idEntidad(idEntidad)
                .descripcion(descripcion)
                .fechaHora(LocalDateTime.now())
                .build();

        auditoriaRepository.save(auditoria);
        log.info("Auditoria registrada: accion={}, entidad={}, idEntidad={}", accion, entidad, idEntidad);
    }

    private Usuario obtenerUsuarioSistema() {
        return usuarioRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No existe un usuario registrado para asociar la operacion"));
    }

}
