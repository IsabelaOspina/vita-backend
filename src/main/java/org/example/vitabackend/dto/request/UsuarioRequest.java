package org.example.vitabackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record UsuarioRequest(
        @NotBlank String cedula,
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        String telefono,
        @Past LocalDate fechaNacimiento,
        String direccion,
        @Valid ContactoEmergenciaRequest contactoEmergencia,
        @Valid List<DiscapacidadRequest> discapacidades,
        @Valid ConfiguracionAccesibilidadRequest configuracionAccesibilidad) {
}
