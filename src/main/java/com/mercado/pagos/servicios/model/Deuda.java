package com.mercado.pagos.servicios.model;

import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.TipoGeneracionDeuda;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deuda")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_deuda")
    private Long id;

    @Column(name = "codigo_deuda", nullable = false, unique = true, length = 30)
    private String codigoDeuda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_concepto_cobro", nullable = false)
    private ConceptoCobro conceptoCobro;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_socio", nullable = false)
    private Socio socio;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_generacion", nullable = false, length = 20)
    private TipoGeneracionDeuda tipoGeneracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoDeuda estado;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "observacion", length = 255)
    private String observacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creado_por", nullable = false)
    private Usuario creadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actualizado_por")
    private Usuario actualizadoPor;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "motivo_actualizacion", length = 255)
    private String motivoActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exonerado_por")
    private Usuario exoneradoPor;

    @Column(name = "fecha_exoneracion")
    private LocalDateTime fechaExoneracion;

    @Column(name = "motivo_exoneracion", length = 255)
    private String motivoExoneracion;

}
