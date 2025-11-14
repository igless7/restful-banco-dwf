package com.bancoagricultura.banco_agricultura.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "accion_personal")
public class AccionPersonal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empleado_usuario_id", nullable = false)
    private Usuario empleadoUsuario;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "tipo_accion", nullable = false)
    private TipoAccion tipoAccion;

    @ColumnDefault("'EN_ESPERA'")
    @Lob
    @Column(name = "estado")
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sucursal_id")
    private Sucursal sucursal;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generado_por_usuario_id", nullable = false)
    private Usuario generadoPorUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionado_por_usuario_id")
    private Usuario gestionadoPorUsuario;

    @Size(max = 255)
    @Column(name = "observaciones")
    private String observaciones;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "creado_en")
    private Instant creadoEn;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "actualizado_en")
    private Instant actualizadoEn;

    public enum TipoAccion {
        ALTA_EMPLEADO,
        BAJA_EMPLEADO,
        CAMBIO_ROL
    }
    public enum Estado {
        APROBADO,
        RECHAZADO,
        EN_ESPERA
    }
}