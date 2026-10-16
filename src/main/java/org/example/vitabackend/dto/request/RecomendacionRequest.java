package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Recomendación del profesional para un paciente. sintomaId es opcional: si se envía,
 * debe ser un síntoma de ese paciente.
 */
public record RecomendacionRequest(
        @NotBlank(message = "El mensaje de la recomendación es obligatorio")
        @Size(max = 1000, message = "La recomendación admite máximo 1000 caracteres") String mensaje,
        Long sintomaId) {
}
