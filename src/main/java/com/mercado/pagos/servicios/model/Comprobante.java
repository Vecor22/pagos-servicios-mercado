package com.mercado.pagos.servicios.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comprobante")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comprobante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comprobante")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_pago", nullable = false, unique = true)
    private Pago pago;

    @Column(name = "numero_comprobante", nullable = false, unique = true, length = 30)
    private String numeroComprobante;

    @Column(name = "tipo_comprobante", nullable = false, length = 30)
    private String tipoComprobante;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

}
