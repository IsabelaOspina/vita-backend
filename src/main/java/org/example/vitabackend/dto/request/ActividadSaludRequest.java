package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.TipoActividad;

/** Datos que puede enviar el cliente para crear o editar una actividad de salud. */
public record ActividadSaludRequest(
        @NotBlank(message = "El nombre de la actividad es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
        String nombre,

        @NotNull(message = "El tipo de actividad es obligatorio")
        TipoActividad tipo,

        @NotNull(message = "La fecha y hora son obligatorias")
        LocalDateTime fechaHora,

        @Size(max = 255, message = "La ubicación no puede superar 255 caracteres")
        String ubicacion,

        @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
        String descripcion
) {
}
