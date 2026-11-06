package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import org.example.vitabackend.domain.enums.TipoEmergencia;

public record SolicitudEmergenciaRequest(

        @NotNull(message = "El tipo de emergencia es obligatorio")
        TipoEmergencia tipo,

        @Size(max = 500, message = "La ubicación no puede superar los 500 caracteres")
        String ubicacion,

        @DecimalMin(value = "-90.0", message = "La latitud debe estar entre -90 y 90")
        @DecimalMax(value = "90.0", message = "La latitud debe estar entre -90 y 90")
        BigDecimal latitud,

        @DecimalMin(value = "-180.0", message = "La latitud debe estar entre -180 y 180")
        @DecimalMax(value = "180.0", message = "La latitud debe estar entre -180 y 180")
        BigDecimal longitud
) {
}