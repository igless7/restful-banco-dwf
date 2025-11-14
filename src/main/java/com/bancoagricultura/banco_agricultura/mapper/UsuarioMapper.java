package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.UsuarioCreateDTO;
import com.bancoagricultura.banco_agricultura.dto.UsuarioDTO;
import com.bancoagricultura.banco_agricultura.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // Entidad -> DTO (lectura)
    @Mapping(source = "persona.id", target = "personaId")
    @Mapping(source = "sucursal.id", target = "sucursalId")
    UsuarioDTO toDTO(Usuario usuario);

    // DTO -> Entidad (creación)
    @Mapping(source = "personaId", target = "persona.id")
    @Mapping(source = "sucursalId", target = "sucursal.id")
    Usuario toEntity(UsuarioCreateDTO dto);
}
