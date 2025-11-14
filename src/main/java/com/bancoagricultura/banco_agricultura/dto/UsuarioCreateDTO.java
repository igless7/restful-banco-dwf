package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.Usuario;
import lombok.Data;

@Data
public class UsuarioCreateDTO {
    private String username;
    private String password;
    private Usuario.Rol rol;
    private Long personaId;
    private Long sucursalId;
}

