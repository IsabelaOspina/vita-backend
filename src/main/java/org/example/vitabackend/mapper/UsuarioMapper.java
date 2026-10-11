package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "contactoEmergencia", ignore = true)
    @Mapping(target = "discapacidades", ignore = true)
    @Mapping(target = "configuracionAccesibilidad", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    UsuarioResponse toResponse(Usuario usuario);
}
