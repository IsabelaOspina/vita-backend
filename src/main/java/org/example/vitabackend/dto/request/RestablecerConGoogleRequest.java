package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * "Olvidé mi contraseña" verificando el correo con Google (en lugar del código por correo):
 * el token de identidad de Google demuestra que la persona es dueña del correo.
 */
public record RestablecerConGoogleRequest(
        @NotBlank(message = "El token de Google es obligatorio") String idToken,
        @NotBlank(message = "Debe indicar la nueva contraseña")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String contrasenaNueva) {
}
