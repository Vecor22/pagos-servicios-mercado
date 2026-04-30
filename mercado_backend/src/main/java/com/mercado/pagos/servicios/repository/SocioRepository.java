package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocioRepository extends JpaRepository<Socio, Long> {

    boolean existsByDni(String dni);

    boolean existsByCodigoSocio(String codigoSocio);

    Optional<Socio> findByCodigoSocio(String codigoSocio);

    List<Socio> findByEstado(EstadoSocio estado);

    Page<Socio> findAllBy(Pageable pageable);

    Page<Socio> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCaseOrDniContaining(
            String nombres,
            String apellidos,
            String dni,
            Pageable pageable
    );

}
