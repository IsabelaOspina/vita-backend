package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;

/** La coherencia entre campos dependientes del tipo de frecuencia se valida en el servicio. */
public record HorarioMedicamentoRequest(
        @NotNull(message = "El tipo de frecuencia es obligatorio") TipoFrecuenciaMedicamento tipoFrecuencia,
        @NotNull(message = "La fecha y hora de inicio son obligatorias") LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        @Min(value = 1, message = "El intervalo mínimo es una hora")
        @Max(value = 168, message = "El intervalo máximo permitido es de 168 horas") Integer intervaloHoras,
        LocalTime horaProgramada,
        Set<DayOfWeek> diasSemana,
        @NotNull(message = "El tipo de recordatorio es obligatorio") TipoRecordatorio tipoRecordatorio
) { }
