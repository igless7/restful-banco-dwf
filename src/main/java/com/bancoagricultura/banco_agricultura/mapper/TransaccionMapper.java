package com.bancoagricultura.banco_agricultura.mapper;

import com.bancoagricultura.banco_agricultura.dto.TransaccionDTO;
import com.bancoagricultura.banco_agricultura.model.Transaccion;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransaccionMapper {

    TransaccionDTO toDTO(Transaccion transaccion);

    Transaccion toEntity(TransaccionDTO dto);
}
