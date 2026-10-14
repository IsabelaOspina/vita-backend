package org.example.vitabackend.mapper;

import org.example.vitabackend.domain.model.Sintoma;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.service.SintomaAnalisis;

/** Conversión manual Sintoma -> respuestas (incluye nivel de intensidad y texto de lectura). */
public final class SintomaMapper {

    private SintomaMapper() {
    }

    public static SintomaResponse aRespuesta(Sintoma s) {
        return new SintomaResponse(
                s.getId(),
                s.getTipo(),
                s.getIntensidad(),
                SintomaAnalisis.nivel(s.getIntensidad()),
                s.getFechaHora(),
                s.getObservaciones(),
                s.getFechaRegistro(),
                s.getFechaActualizacion(),
                SintomaAnalisis.textoRegistro(s.getTipo(), s.getIntensidad(), s.getFechaHora(), s.getObservaciones()));
    }

    public static SintomaAnalisis.Registro aRegistro(Sintoma s) {
        return new SintomaAnalisis.Registro(s.getFechaHora(), s.getTipo(), s.getIntensidad());
    }
}
