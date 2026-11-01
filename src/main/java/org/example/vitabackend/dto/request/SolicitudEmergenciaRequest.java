package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.vitabackend.domain.enums.TipoEmergencia;

public record SolicitudEmergenciaRequest(

        @NotNull(message = "El tipo de emergencia es obligatorio")
        TipoEmergencia tipo,

        @Size(max = 500, message = "La ubicación no puede superar los 500 caracteres")
        String ubicacion

) {
}