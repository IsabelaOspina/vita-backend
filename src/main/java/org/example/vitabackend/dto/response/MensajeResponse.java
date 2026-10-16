package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;

/** Respuesta simple con un mensaje legible para el usuario (misma forma que los errores). */
public record MensajeResponse(LocalDateTime timestamp, int status, String mensaje) {

    public static MensajeResponse ok(String mensaje) {
        return new MensajeResponse(LocalDateTime.now(), 200, mensaje);
    }
}
