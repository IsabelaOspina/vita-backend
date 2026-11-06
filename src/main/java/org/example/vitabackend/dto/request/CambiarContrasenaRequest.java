package org.example.vitabackend.dto.request;

import org.example.vitabackend.validation.ContrasenaValida;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarContrasenaRequest(
        @NotBlank(message = "Debe indicar la contraseña actual") String contrasenaActual,
        @NotBlank(message = "Debe indicar la nueva contraseña")
        @ContrasenaValida 
        String contrasenaNueva
) {
}
