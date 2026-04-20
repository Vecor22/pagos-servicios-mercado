package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {

    Optional<Comprobante> findByNumeroComprobante(String numeroComprobante);

    Optional<Comprobante> findByPagoId(Long pagoId);

}
