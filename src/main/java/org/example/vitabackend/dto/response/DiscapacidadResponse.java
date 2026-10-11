package org.example.vitabackend.dto.response;

import org.example.vitabackend.domain.enums.TipoDiscapacidad;

public record DiscapacidadResponse(
        Long id,
        TipoDiscapacidad tipo,
        String descripcion) {
}
