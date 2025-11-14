package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.ComisionColaboradorDTO;
import com.bancoagricultura.banco_agricultura.model.ComisionColaborador;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ComisionColaboradorMapper {

    @Mapping(source = "colaboradorUsuario.id", target = "colaboradorUsuarioId")
    ComisionColaboradorDTO toDTO(ComisionColaborador comision);

    @Mapping(source = "colaboradorUsuarioId", target = "colaboradorUsuario.id")
    ComisionColaborador toEntity(ComisionColaboradorDTO dto);
}
