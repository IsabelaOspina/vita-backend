package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.response.ActividadSaludResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/** Convierte DTO a entidades y entidades a DTO sin exponer relaciones internas. */
@Mapper(componentModel = "spring")
public interface ActividadSaludMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "estado", ignore = true)
    ActividadSalud toEntity(ActividadSaludRequest request);

    ActividadSaludResponse toResponse(ActividadSalud actividadSalud);

    /** Actualiza solo los campos editables; conserva propietario, identificador y estado. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "estado", ignore = true)
    void actualizarDesdeRequest(ActividadSaludRequest request, @MappingTarget ActividadSalud actividadSalud);
}
