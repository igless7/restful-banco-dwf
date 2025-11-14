package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.CuentaDTO;
import com.bancoagricultura.banco_agricultura.model.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CuentaMapper {

    @Mapping(source = "clienteUsuario.id", target = "clienteUsuarioId")
    @Mapping(source = "sucursal.id", target = "sucursalId")
    CuentaDTO toDTO(Cuenta cuenta);

    @Mapping(source = "clienteUsuarioId", target = "clienteUsuario.id")
    @Mapping(source = "sucursalId", target = "sucursal.id")
    Cuenta toEntity(CuentaDTO cuentaDTO);
}
