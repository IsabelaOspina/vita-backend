package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.vitabackend.domain.enums.Rol;

public record PersonalRequest(
        @NotBlank String cedula,
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        String telefono,
        @NotNull(message = "Debe indicar el rol") Rol rol) {
}
