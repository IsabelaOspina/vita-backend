package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para que el administrador registre a un profesional de la salud.
 * El rol no se envía: siempre es PERSONAL_SALUD.
 */
public record PersonalRequest(
        @NotBlank String cedula,
        @NotBlank String nombre,
        @NotBlank @Email String correo,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String contrasena,
        String telefono,
        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad admite máximo 100 caracteres") String especialidad,
        @NotBlank(message = "El número de registro profesional es obligatorio")
        @Size(max = 50, message = "El número de registro admite máximo 50 caracteres") String numeroRegistroProfesional) {
}
