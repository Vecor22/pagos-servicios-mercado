package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocioPuestoRepository extends JpaRepository<SocioPuesto, Long> {

    boolean existsByPuestoId(Long puestoId);

    Optional<SocioPuesto> findByPuestoId(Long puestoId);

    List<SocioPuesto> findBySocioId(Long socioId);

    List<SocioPuesto> findBySocioDni(String dni);

}
