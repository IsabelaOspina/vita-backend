package org.example.vitabackend.dto.response;

import java.time.LocalDate;
import java.util.List;

/**
 * Indicadores del periodo: número de síntomas registrados, días con registro e intensidad.
 * Los promedios, máximo y mínimo son null cuando no hay registros.
 */
public record SintomaResumenResponse(
        LocalDate desde,
        LocalDate hasta,
        long totalRegistros,
        long diasConRegistro,
        Double intensidadPromedio,
        Integer intensidadMaxima,
        Integer intensidadMinima,
        List<TipoResumen> porTipo,
        String textoLectura) {

    /** Resumen de un tipo de síntoma dentro del periodo. */
    public record TipoResumen(
            String tipo,
            long cantidad,
            double intensidadPromedio,
            int intensidadMaxima,
            java.time.LocalDateTime ultimoRegistro,
            int ultimaIntensidad) {
    }
}
