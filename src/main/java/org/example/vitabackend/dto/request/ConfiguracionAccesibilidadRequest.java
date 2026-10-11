package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Todos los campos son opcionales. Lo que no se envíe se completa con
 * valores por defecto según las discapacidades declaradas.
 */
public record ConfiguracionAccesibilidadRequest(
        Boolean altoContraste,
        @Min(value = 12, message = "El tamaño de texto mínimo es 12")
        @Max(value = 40, message = "El tamaño de texto máximo es 40") Integer tamanoTexto,
        Boolean lectorPantalla,
        Boolean navegacionSimple,
        Boolean notificacionesVoz,
        Boolean notificacionesSonoras,
        Boolean notificacionesVisuales) {
}
