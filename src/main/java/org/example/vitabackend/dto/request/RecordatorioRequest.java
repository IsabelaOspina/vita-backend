package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.TipoRecordatorio;

/** Datos para programar un recordatorio vinculado a una actividad de salud. */
public record RecordatorioRequest(
        @NotNull(message = "La fecha y hora del recordatorio son obligatorias")
        LocalDateTime fechaHora,
        @NotBlank(message = "El mensaje del recordatorio es obligatorio")
        @Size(max = 255, message = "El mensaje no puede superar 255 caracteres")
        String mensaje,
        @NotNull(message = "El tipo de recordatorio es obligatorio")
        TipoRecordatorio tipoRecordatorio
) { }
