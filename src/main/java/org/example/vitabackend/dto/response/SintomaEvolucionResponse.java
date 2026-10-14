package org.example.vitabackend.dto.response;

import java.time.LocalDate;
import java.util.List;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.Tendencia;

/**
 * Evolución de la intensidad agrupada por día, semana o mes. Solo se incluyen los periodos
 * que tienen registros.
 * tipo es null si se consultaron todos los síntomas juntos.
 */
public record SintomaEvolucionResponse(
        String tipo,
        LocalDate desde,
        LocalDate hasta,
        AgrupacionTiempo agrupacion,
        List<Punto> puntos,
        Tendencia tendencia,
        String textoLectura) {

    /** Un periodo [inicio, fin] (ambos inclusive) con sus estadísticas. */
    public record Punto(
            LocalDate inicio,
            LocalDate fin,
            long cantidad,
            double intensidadPromedio,
            int intensidadMaxima) {
    }
}
