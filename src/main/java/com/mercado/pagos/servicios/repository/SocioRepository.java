package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocioRepository extends JpaRepository<Socio, Long> {

    boolean existsByDni(String dni);

    boolean existsByCodigoSocio(String codigoSocio);

    List<Socio> findByEstado(EstadoSocio estado);

    List<Socio> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombres, String apellidos);

}
