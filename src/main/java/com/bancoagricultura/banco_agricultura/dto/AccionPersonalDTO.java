package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.AccionPersonal;

public class AccionPersonalDTO {
    private Long id;
    private AccionPersonal.TipoAccion tipoAccion;
    private AccionPersonal.Estado estado;
    private String observaciones;
    private Long empleadoUsuarioId;
}
