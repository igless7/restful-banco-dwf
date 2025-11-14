package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.Prestamo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PrestamoDTO {
    private Long id;
    private BigDecimal montoAprobado;
    private BigDecimal tasaInteres;
    private BigDecimal saldoPendiente;
    private Integer plazoAnios;
    private BigDecimal cuotaMensual;
    private LocalDate fechaVencimiento;
    private LocalDate proximoPago;
    private Prestamo.Estado estado;
    private Long usuarioClienteId;
}
