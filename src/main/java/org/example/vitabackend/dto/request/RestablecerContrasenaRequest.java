package org.example.vitabackend.dto.request;

import org.example.vitabackend.validation.ContrasenaValida;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Código de 6 dígitos que llegó al correo y la nueva contraseña.
public record RestablecerContrasenaRequest(
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido") String correo,
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "\\d{6}", message = "El código debe tener 6 dígitos") String codigo,
        @NotBlank(message = "Debe indicar la nueva contraseña")
        @ContrasenaValida 
        String contrasenaNueva
) {
}
