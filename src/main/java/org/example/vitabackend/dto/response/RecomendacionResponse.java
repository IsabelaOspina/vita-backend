package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;

/** textoLectura: frase lista para leer en voz alta o mostrar en texto grande. */
public record RecomendacionResponse(
        Long id,
        String mensaje,
        Long profesionalId,
        String profesionalNombre,
        String especialidad,
        Long pacienteId,
        Long sintomaId,
        String sintomaReferencia,
        LocalDateTime fechaCreacion,
        Boolean leida,
        LocalDateTime fechaLectura,
        String textoLectura) {
}
