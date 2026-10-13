package org.example.vitabackend.dto.response;

public record PersonalSaludResponse(
        Long id,
        String especialidad,
        String numeroRegistroProfesional) {
}
