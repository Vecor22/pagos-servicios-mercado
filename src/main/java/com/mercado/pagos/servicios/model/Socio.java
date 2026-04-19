package com.mercado.pagos.servicios.model;

import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "socio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Socio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_socio")
    private Long id;

    @Column(name = "codigo_socio", nullable = false, unique = true, length = 20)
    private String codigoSocio;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "correo", length = 100)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoSocio estado;

}
