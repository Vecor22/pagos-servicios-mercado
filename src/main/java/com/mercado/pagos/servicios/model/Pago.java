package com.mercado.pagos.servicios.model;

import com.mercado.pagos.servicios.model.enums.EstadoPago;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long id;

    @Column(name = "codigo_pago", nullable = false, unique = true, length = 30)
    private String codigoPago;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_deuda", nullable = false, unique = true)
    private Deuda deuda;

    @Column(name = "monto_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPagado;

    @Column(name = "medio_pago", nullable = false, length = 30)
    private String medioPago;

    @Column(name = "numero_operacion", length = 50)
    private String numeroOperacion;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoPago estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "registrado_por", nullable = false)
    private Usuario registradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por")
    private Usuario anuladoPor;

    @Column(name = "fecha_anulacion")
    private LocalDateTime fechaAnulacion;

    @Column(name = "motivo_anulacion", length = 255)
    private String motivoAnulacion;

}
