package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.SolicitudPrestamo;

import java.math.BigDecimal;

public class SolicitudPrestamoDTO {
    private Long id;
    private BigDecimal montoSolicitado;
    private SolicitudPrestamo.EstadoCaso estadoCaso;
    private String observaciones;
    private Long usuarioClienteId;
}
