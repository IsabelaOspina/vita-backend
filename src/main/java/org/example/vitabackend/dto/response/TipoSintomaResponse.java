package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;

/** Un tipo de síntoma que el paciente ya ha registrado (para sugerirlo al escribir o dictar). */
public record TipoSintomaResponse(String tipo, long cantidad, LocalDateTime ultimoRegistro) {
}
