package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByEntidad(String entidad);

    List<Auditoria> findByFechaHoraBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
