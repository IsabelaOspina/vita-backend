package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
        @NotBlank(message = "Debe indicar la contraseña actual") String contrasenaActual,
        @NotBlank(message = "Debe indicar la nueva contraseña")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String contrasenaNueva) {
}
