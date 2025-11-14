package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.Cuenta;

import java.math.BigDecimal;

public class CuentaDTO {
    private Long id;
    private String numeroCuenta;
    private Cuenta.TipoCuenta tipoCuenta;
    private BigDecimal saldoDisponible;
    private Boolean activa;
    private Long clienteUsuarioId;
    private Long sucursalId;
}
