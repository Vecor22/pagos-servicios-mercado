package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {

    boolean existsByCodigoDeuda(String codigoDeuda);

    List<Deuda> findByPuestoId(Long puestoId);

    List<Deuda> findByPuestoCodigoPuesto(String codigoPuesto);

    List<Deuda> findByPuestoCodigoPuestoAndEstado(String codigoPuesto, EstadoDeuda estado);

    List<Deuda> findByPuestoIdAndFechaGeneracionBetween(
            Long puestoId,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin
    );

    List<Deuda> findByEstado(EstadoDeuda estado);

    List<Deuda> findByFechaGeneracionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
