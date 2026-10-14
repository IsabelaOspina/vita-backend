package org.example.vitabackend.dto.request;

import java.time.LocalDate;

/** Filtros opcionales del historial. Las fechas son días completos (yyyy-MM-dd). */
public record SintomaFiltro(
        LocalDate desde,
        LocalDate hasta,
        String tipo,
        Integer intensidadMin,
        Integer intensidadMax) {
}
