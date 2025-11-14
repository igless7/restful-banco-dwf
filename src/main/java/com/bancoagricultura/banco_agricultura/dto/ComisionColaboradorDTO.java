package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.ComisionColaborador;

import java.math.BigDecimal;
import java.time.Instant;

public class ComisionColaboradorDTO {
    private Long id;
    private BigDecimal montoComision;
    private BigDecimal porcentajeComision;
    private Instant fechaComision;
    private ComisionColaborador.Estado estado;
    private Long colaboradorUsuarioId;
}
