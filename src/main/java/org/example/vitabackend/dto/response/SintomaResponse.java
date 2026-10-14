package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.NivelIntensidad;

/**
 * textoLectura es una frase lista para que el frontend la lea en voz alta o la muestre
 * en texto grande; nivelIntensidad sirve para colores/alto contraste.
 */
public record SintomaResponse(
        Long id,
        String tipo,
        Integer intensidad,
        NivelIntensidad nivelIntensidad,
        LocalDateTime fechaHora,
        String observaciones,
        LocalDateTime fechaRegistro,
        LocalDateTime fechaActualizacion,
        String textoLectura) {
}
