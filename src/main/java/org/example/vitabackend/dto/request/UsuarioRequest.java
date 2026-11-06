package org.example.vitabackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import org.example.vitabackend.validation.ContrasenaValida;

public record UsuarioRequest(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{7,15}", message = "La cédula debe tener entre 7 y 15 dígitos y solo números") String cedula,
        @NotBlank(message = "El nombre es obligatorio")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$",
                message = "El nombre solo puede contener letras y espacios"
        )
        String nombre,
        @NotBlank @Email String correo,
        @NotBlank (message = "La contraseña es obligatoria") 
        @ContrasenaValida 
        String contrasena,
        @Pattern(regexp = "\\d{10}", message = "El teléfono debe tener exactamente 10 dígitos y solo números") String telefono,
        @Past LocalDate fechaNacimiento,
        String direccion,
        @Valid ContactoEmergenciaRequest contactoEmergencia,
        @Valid List<DiscapacidadRequest> discapacidades,
        @Valid ConfiguracionAccesibilidadRequest configuracionAccesibilidad,
        @NotNull (message = "Debe indicar si acepta el tratamiento de datos personales")
        @AssertTrue (message = "Debe aceptar el tratamiento de sus datos personales para crear una cuenta")
        Boolean aceptaTratamientoDatosPersonales) {
}
