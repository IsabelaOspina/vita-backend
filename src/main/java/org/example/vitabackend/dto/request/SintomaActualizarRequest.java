package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Editar un síntoma (RF:15). Reemplaza tipo, intensidad, fecha/hora y observaciones.
 * Si no se envía fechaHora se conserva la que ya tenía.
 */
public record SintomaActualizarRequest(
        @NotBlank(message = "El tipo de síntoma es obligatorio")
        @Size(max = 100, message = "El tipo de síntoma admite máximo 100 caracteres") String tipo,
        @NotNull(message = "La intensidad es obligatoria")
        @Min(value = 1, message = "La intensidad mínima es 1")
        @Max(value = 10, message = "La intensidad máxima es 10") Integer intensidad,
        LocalDateTime fechaHora,
        @Size(max = 1000, message = "Las observaciones admiten máximo 1000 caracteres") String observaciones) {
}
