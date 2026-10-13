package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;

/**
 * Una asignación paciente-profesional. Los ids son los de las cuentas (usuario.id).
 */
public record AsignacionResponse(
        Long id,
        Long pacienteId,
        String pacienteNombre,
        Long profesionalId,
        String profesionalNombre,
        String especialidad,
        String numeroRegistroProfesional,
        LocalDateTime fechaAsignacion) {
}
