package com.mercado.pagos.servicios.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "socio_puesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocioPuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio_puesto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_puesto", nullable = false, unique = true)
    private Puesto puesto;

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDate fechaAsignacion;

}
