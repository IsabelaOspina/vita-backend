package org.example.vitabackend.dto.response;

public record ContactoEmergenciaResponse(
        Long id,
        String nombre,
        String telefono,
        String parentesco) {
}
