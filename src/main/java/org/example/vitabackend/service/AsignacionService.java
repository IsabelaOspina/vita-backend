package org.example.vitabackend.service;

import java.util.List;
import org.example.vitabackend.dto.response.AsignacionResponse;

public interface AsignacionService {

    AsignacionResponse asignar(Long profesionalId, Long pacienteId);

    void desasignar(Long profesionalId, Long pacienteId);

    /** Pacientes asignados a un profesional (profesionalId = id de su cuenta). */
    List<AsignacionResponse> pacientesDe(Long profesionalId);

    /** Profesionales asignados a un paciente (pacienteId = id de su cuenta). */
    List<AsignacionResponse> profesionalesDe(Long pacienteId);
}
