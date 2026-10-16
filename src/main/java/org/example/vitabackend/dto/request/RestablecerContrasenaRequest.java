package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Paso 2: el código de 6 dígitos que llegó al correo y la nueva contraseña. */
public record RestablecerContrasenaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido") String correo,
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos") String codigo,
        @NotBlank(message = "Debe indicar la nueva contraseña")
        @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres") String contrasenaNueva) {
}
