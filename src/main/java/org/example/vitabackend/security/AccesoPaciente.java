package org.example.vitabackend.security;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.repository.AsignacionPacienteRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/** Regla de acceso: un profesional solo ve datos de los pacientes que el admin le asignó. */
@Component
@RequiredArgsConstructor
public class AccesoPaciente {

    private final AsignacionPacienteRepository asignacionRepository;

    /** Lanza AccessDeniedException (403) si el paciente no está asignado a ese profesional. */
    public void verificarAsignado(Long profesionalId, Long pacienteId) {
        if (!asignacionRepository.existsByPersonalSalud_Usuario_IdAndPaciente_Id(profesionalId, pacienteId)) {
            throw new AccessDeniedException("El paciente no está asignado a usted");
        }
    }
}
