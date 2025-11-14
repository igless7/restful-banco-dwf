package com.bancoagricultura.banco_agricultura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "cuenta")
public class Cuenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 20)
    @NotNull
    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_usuario_id", nullable = false)
    private Usuario clienteUsuario;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo_cuenta", nullable = false)
    private TipoCuenta tipoCuenta = TipoCuenta.CORRIENTE;

    @ColumnDefault("0.00")
    @Column(name = "saldo", precision = 14, scale = 2)
    private BigDecimal saldo;

    @ColumnDefault("0.00")
    @Column(name = "saldo_disponible", precision = 14, scale = 2)
    private BigDecimal saldoDisponible;

    @ColumnDefault("1")
    @Column(name = "activa")
    private Boolean activa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creada_por_usuario_id")
    private Usuario creadaPorUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creada_en")
    private Instant creadaEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "actualizada_en")
    private Instant actualizadaEn;

    public enum TipoCuenta {
        AHORROS,
        CORRIENTE
    }
}