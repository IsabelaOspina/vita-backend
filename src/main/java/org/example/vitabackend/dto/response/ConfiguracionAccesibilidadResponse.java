package org.example.vitabackend.dto.response;

public record ConfiguracionAccesibilidadResponse(
        Boolean altoContraste,
        Integer tamanoTexto,
        Boolean lectorPantalla,
        Boolean navegacionSimple,
        Boolean notificacionesVoz,
        Boolean notificacionesSonoras,
        Boolean notificacionesVisuales) {
}
