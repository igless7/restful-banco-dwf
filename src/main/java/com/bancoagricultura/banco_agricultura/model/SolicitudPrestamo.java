package com.bancoagricultura.banco_agricultura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "solicitud_prestamo")
public class SolicitudPrestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_cliente_id", nullable = false)
    private Usuario usuarioCliente;

    @NotNull
    @Column(name = "monto_solicitado", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoSolicitado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_caso", nullable = false)
    private EstadoCaso estadoCaso = EstadoCaso.EN_ESPERA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id")
    private Cuenta cuentaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitado_por_usuario_id")
    private Usuario solicitadoPorUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionado_por_usuario_id")
    private Usuario gestionadoPorUsuario;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private Instant creadoEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    public enum EstadoCaso {
        EN_ESPERA,
        APROBADO,
        RECHAZADO
    }
}