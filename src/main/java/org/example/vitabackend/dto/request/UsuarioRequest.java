package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UsuarioRequest(
        @NotBlank String cedula,
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        String telefono,
        @Past LocalDate fechaNacimiento,
        String direccion) {
}

