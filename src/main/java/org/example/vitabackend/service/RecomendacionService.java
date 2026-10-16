package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.RecomendacionRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.RecomendacionResponse;

/**
 * Recomendaciones del personal de salud a sus pacientes. Los ids son los de las cuentas (usuario.id).
 */
public interface RecomendacionService {

    /** El profesional deja una recomendación a un paciente que tenga asignado (si no, 403). */
    RecomendacionResponse crear(Long profesionalId, Long pacienteId, RecomendacionRequest request);

    /** Lo que ese profesional le ha escrito a ese paciente (más recientes primero). */
    PaginaResponse<RecomendacionResponse> listarDelProfesional(Long profesionalId, Long pacienteId,
                                                               int pagina, int tamano);

    /** Las recomendaciones que recibió el paciente (más recientes primero). */
    PaginaResponse<RecomendacionResponse> listarDelPaciente(Long pacienteId, boolean soloNoLeidas,
                                                            int pagina, int tamano);

    long contarNoLeidas(Long pacienteId);

    /** El paciente marca una recomendación como leída. Si ya lo estaba, no cambia nada. */
    RecomendacionResponse marcarLeida(Long pacienteId, Long recomendacionId);
}
