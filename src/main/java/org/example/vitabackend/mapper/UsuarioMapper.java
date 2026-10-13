package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.ConfiguracionAccesibilidad;
import org.example.vitabackend.domain.model.ContactoEmergencia;
import org.example.vitabackend.domain.model.Discapacidad;
import org.example.vitabackend.domain.model.PersonalSalud;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ContactoEmergenciaRequest;
import org.example.vitabackend.dto.request.DiscapacidadRequest;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.ConfiguracionAccesibilidadResponse;
import org.example.vitabackend.dto.response.ContactoEmergenciaResponse;
import org.example.vitabackend.dto.response.DiscapacidadResponse;
import org.example.vitabackend.dto.response.PersonalSaludResponse;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // La configuración de accesibilidad la arma ConfiguracionAccesibilidadFactory en el servicio
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "configuracionAccesibilidad", ignore = true)
    @Mapping(target = "personalSalud", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    Discapacidad discapacidadToEntity(DiscapacidadRequest request);

    @Mapping(target = "id", ignore = true)
    ContactoEmergencia contactoToEntity(ContactoEmergenciaRequest request);

    UsuarioResponse toResponse(Usuario usuario);

    DiscapacidadResponse discapacidadToResponse(Discapacidad discapacidad);

    ContactoEmergenciaResponse contactoToResponse(ContactoEmergencia contacto);

    ConfiguracionAccesibilidadResponse configuracionToResponse(ConfiguracionAccesibilidad configuracion);

    PersonalSaludResponse personalSaludToResponse(PersonalSalud personalSalud);
}
