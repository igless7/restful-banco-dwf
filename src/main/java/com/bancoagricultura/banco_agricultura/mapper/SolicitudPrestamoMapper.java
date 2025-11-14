package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.SolicitudPrestamoDTO;
import com.bancoagricultura.banco_agricultura.model.SolicitudPrestamo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudPrestamoMapper {

    @Mapping(source = "usuarioCliente.id", target = "usuarioClienteId")
    SolicitudPrestamoDTO toDTO(SolicitudPrestamo solicitud);

    @Mapping(source = "usuarioClienteId", target = "usuarioCliente.id")
    SolicitudPrestamo toEntity(SolicitudPrestamoDTO dto);
}

