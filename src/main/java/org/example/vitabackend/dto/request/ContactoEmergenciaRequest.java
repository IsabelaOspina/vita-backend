package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactoEmergenciaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,
        @NotBlank(message = "El teléfono del contacto es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El teléfono del contacto debe tener exactamente 10 dígitos y solo números") String telefono,
        @NotBlank(message = "El parentesco es obligatorio")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$",
                message = "El parentesco solo puede contener letras y espacios"
        )
        String parentesco) {
}
