package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.TipoActividad;

/** Representación pública de una actividad; no expone la entidad Usuario. */
public record ActividadSaludResponse(
        Long id,
        String nombre,
        TipoActividad tipo,
        LocalDateTime fechaHora,
        String ubicacion,
        String descripcion,
        EstadoActividad estado
) {
}
