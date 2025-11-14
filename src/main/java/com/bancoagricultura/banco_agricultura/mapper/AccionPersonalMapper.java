package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.AccionPersonalDTO;
import com.bancoagricultura.banco_agricultura.model.AccionPersonal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccionPersonalMapper {

    @Mapping(source = "empleadoUsuario.id", target = "empleadoUsuarioId")
    AccionPersonalDTO toDTO(AccionPersonal accion);

    @Mapping(source = "empleadoUsuarioId", target = "empleadoUsuario.id")
    AccionPersonal toEntity(AccionPersonalDTO dto);
}
