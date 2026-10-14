package org.example.vitabackend.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Hora de VITA. Las fechas viajan sin zona horaria (ej. 2026-10-20T09:30:00),
 * (por defecto America/Bogota; se puede cambiar con vita.zona-horaria).
 */
@Component
public class Reloj {

    private final ZoneId zona;

    public Reloj(@Value("${vita.zona-horaria:America/Bogota}") String zona) {
        this.zona = ZoneId.of(zona);
    }

    public LocalDateTime ahora() {
        return LocalDateTime.now(zona);
    }
}
