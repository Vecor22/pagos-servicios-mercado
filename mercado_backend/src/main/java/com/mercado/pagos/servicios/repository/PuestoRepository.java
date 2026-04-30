package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    boolean existsByCodigoPuesto(String codigoPuesto);

    Optional<Puesto> findByCodigoPuesto(String codigoPuesto);

}
