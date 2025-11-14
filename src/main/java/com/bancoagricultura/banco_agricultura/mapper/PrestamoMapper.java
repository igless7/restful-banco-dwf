package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.PrestamoDTO;
import com.bancoagricultura.banco_agricultura.model.Prestamo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrestamoMapper {

    @Mapping(source = "usuarioCliente.id", target = "usuarioClienteId")
    PrestamoDTO toDTO(Prestamo prestamo);

    @Mapping(source = "usuarioClienteId", target = "usuarioCliente.id")
    Prestamo toEntity(PrestamoDTO dto);
}

