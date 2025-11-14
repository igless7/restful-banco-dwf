package com.bancoagricultura.banco_agricultura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "prestamo")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitud_prestamo_id", nullable = false)
    private SolicitudPrestamo solicitudPrestamo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_cliente_id", nullable = false)
    private Usuario usuarioCliente;

    @NotNull
    @Column(name = "monto_aprobado", nullable = false, precision = 14, scale = 2)
    private BigDecimal montoAprobado;

    @NotNull
    @Column(name = "tasa_interes", nullable = false, precision = 5, scale = 4)
    private BigDecimal tasaInteres;

    @NotNull
    @Column(name = "saldo_pendiente", nullable = false, precision = 14, scale = 2)
    private BigDecimal saldoPendiente;

    @NotNull
    @Column(name = "plazo_anios", nullable = false)
    private Integer plazoAnios;

    @Column(name = "cuota_mensual", precision = 14, scale = 2)
    private BigDecimal cuotaMensual;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_aprobacion")
    private Instant fechaAprobacion;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "proximo_pago")
    private LocalDate proximoPago;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_destino_id", nullable = false)
    private Cuenta cuentaDestino;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aprobado_por_usuario_id", nullable = false)
    private Usuario aprobadoPorUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private Instant creadoEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    public enum Estado {
        ACTIVO,
        CANCELADO
    }
}