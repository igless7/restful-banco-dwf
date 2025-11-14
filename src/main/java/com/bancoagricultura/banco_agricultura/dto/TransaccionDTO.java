package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.Transaccion;

import java.math.BigDecimal;
import java.time.Instant;

public class TransaccionDTO {
    private Long id;
    private Transaccion.Tipo tipo;
    private BigDecimal monto;
    private BigDecimal comision;
    private Instant fechaHora;
    private String referencia;
}
