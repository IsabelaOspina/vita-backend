package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Paso 1 de "olvidé mi contraseña": el correo con el que se registró. */
public record OlvideContrasenaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido") String correo) {
}
