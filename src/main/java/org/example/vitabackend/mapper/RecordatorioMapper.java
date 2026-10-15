package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.dto.response.RecordatorioResponse;
import org.springframework.stereotype.Component;

/** Mapeo explícito para evitar exponer entidades JPA y relaciones lazy en la API. */
@Component
public class RecordatorioMapper {

    public RecordatorioResponse toResponse(Recordatorio recordatorio) {
        return new RecordatorioResponse(
                recordatorio.getId(),
                recordatorio.getFechaHora(),
                recordatorio.getMensaje(),
                recordatorio.getTipoRecordatorio(),
                recordatorio.getEstado(),
                recordatorio.getActividadSalud() == null ? null : recordatorio.getActividadSalud().getId(),
                recordatorio.getTomaMedicamento() == null ? null : recordatorio.getTomaMedicamento().getId());
    }
}
