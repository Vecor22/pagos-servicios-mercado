package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocioPuestoRepository extends JpaRepository<SocioPuesto, Long> {

    boolean existsByPuestoId(Long puestoId);

    Optional<SocioPuesto> findByPuestoId(Long puestoId);

    Optional<SocioPuesto> findByPuestoCodigoPuesto(String codigoPuesto);

    List<SocioPuesto> findBySocioId(Long socioId);

    List<SocioPuesto> findBySocioDni(String dni);

    Page<SocioPuesto> findAllBy(Pageable pageable);

}
