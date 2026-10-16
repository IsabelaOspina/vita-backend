package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos para que el administrador registre a un profesional de la salud.
 * El rol no se envía: siempre es PERSONAL_SALUD.
 */
public record PersonalRequest(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{7,15}", message = "La cédula debe tener entre 7 y 15 dígitos y solo números") String cedula,
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener exactamente 10 dígitos y solo números") String telefono,
        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad admite máximo 100 caracteres") String especialidad,
        @NotBlank(message = "El número de registro profesional es obligatorio")
        @Size(max = 50, message = "El número de registro admite máximo 50 caracteres") String numeroRegistroProfesional) {
}
