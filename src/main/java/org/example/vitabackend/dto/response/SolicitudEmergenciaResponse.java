package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.EstadoEmergencia;
import org.example.vitabackend.domain.enums.TipoEmergencia;

public record SolicitudEmergenciaResponse(
        Long id,
        LocalDateTime fechaHora,
        TipoEmergencia tipo,
        String ubicacion,
        EstadoEmergencia estado,
        String nombreContacto,
        String telefonoContacto
) {
}