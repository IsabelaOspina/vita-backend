package org.example.vitabackend.dto.response;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;

public record HorarioMedicamentoResponse(
        Long id, Long medicamentoId, TipoFrecuenciaMedicamento tipoFrecuencia,
        LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin, Integer intervaloHoras,
        LocalTime horaProgramada, Set<DayOfWeek> diasSemana, TipoRecordatorio tipoRecordatorio,
        Boolean activo
) { }
