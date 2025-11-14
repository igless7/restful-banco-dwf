package com.bancoagricultura.banco_agricultura.dto;

import com.bancoagricultura.banco_agricultura.model.Usuario;
import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String username;
    private Usuario.Rol rol;
    private Usuario.Estado estado;
    private Long personaId;
    private Long sucursalId;
}
