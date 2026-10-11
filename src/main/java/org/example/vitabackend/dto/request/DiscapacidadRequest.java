package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.vitabackend.domain.enums.TipoDiscapacidad;

public record DiscapacidadRequest(
        @NotNull(message = "El tipo de discapacidad es obligatorio") TipoDiscapacidad tipo,
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres") String descripcion) {
}
