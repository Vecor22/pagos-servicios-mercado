package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Pago> findByDeudaPuestoCodigoPuesto(String codigoPuesto);

    Optional<Pago> findByDeudaId(Long deudaId);

    List<Pago> findByEstado(EstadoPago estado);

    List<Pago> findByEstadoAndFechaPagoBetween(EstadoPago estado, LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
