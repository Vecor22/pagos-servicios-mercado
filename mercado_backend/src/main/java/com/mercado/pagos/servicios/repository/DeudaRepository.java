package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {

    boolean existsByCodigoDeuda(String codigoDeuda);

    Optional<Deuda> findByCodigoDeuda(String codigoDeuda);

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

    Page<Deuda> findAllBy(Pageable pageable);

}
