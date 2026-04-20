package com.mercado.pagos.servicios.repository;

import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.enums.TipoCobro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConceptoCobroRepository extends JpaRepository<ConceptoCobro, Long> {

    List<ConceptoCobro> findByTipoCobro(TipoCobro tipoCobro);

}
