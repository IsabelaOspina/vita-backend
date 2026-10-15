package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicamentoRequest(
        @NotBlank(message = "El nombre del medicamento es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar 150 caracteres") String nombre,
        @Size(max = 100, message = "La dosis no puede superar 100 caracteres") String dosis,
        @Size(max = 120, message = "La frecuencia descriptiva no puede superar 120 caracteres") String frecuencia,
        @Size(max = 100, message = "La vía de administración no puede superar 100 caracteres") String viaAdministracion
) { }
