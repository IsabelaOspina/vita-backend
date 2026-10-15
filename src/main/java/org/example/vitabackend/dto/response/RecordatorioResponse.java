package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.TipoRecordatorio;

/** DTO de lectura para sincronizar recordatorios con las notificaciones locales del dispositivo. */
public record RecordatorioResponse(
        Long id,
        LocalDateTime fechaHora,
        String mensaje,
        TipoRecordatorio tipoRecordatorio,
        EstadoRecordatorio estado,
        Long actividadSaludId,
        Long tomaMedicamentoId
) { }
