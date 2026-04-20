package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByFechaPagoBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Pago> findByEstado(EstadoPago estado);

}
