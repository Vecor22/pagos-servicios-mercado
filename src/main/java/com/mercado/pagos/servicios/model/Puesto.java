package com.mercado.pagos.servicios.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto")
    private Long id;

    @Column(name = "codigo_puesto", nullable = false, unique = true, length = 20)
    private String codigoPuesto;

}
