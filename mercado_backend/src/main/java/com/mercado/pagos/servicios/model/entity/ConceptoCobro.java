package com.mercado.pagos.servicios.model.entity;

import com.mercado.pagos.servicios.model.enums.TipoCobro;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "concepto_cobro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConceptoCobro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_concepto_cobro")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobro", nullable = false, length = 20)
    private TipoCobro tipoCobro;

}
