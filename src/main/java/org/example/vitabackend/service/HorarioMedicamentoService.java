package org.example.vitabackend.service;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.dto.request.HorarioMedicamentoRequest;
import org.example.vitabackend.dto.response.HorarioMedicamentoResponse;
import org.example.vitabackend.dto.response.TomaMedicamentoResponse;

public interface HorarioMedicamentoService {
    HorarioMedicamentoResponse crear(String correo, Long medicamentoId, HorarioMedicamentoRequest request);
    List<HorarioMedicamentoResponse> listar(String correo, Long medicamentoId);
    void cancelar(String correo, Long medicamentoId, Long horarioId);
    List<TomaMedicamentoResponse> listarTomas(String correo, Long medicamentoId, Long horarioId, LocalDateTime desde, LocalDateTime hasta);

    /** Todas las tomas del usuario en un rango (de sus medicamentos y horarios activos), en una sola consulta. */
    List<TomaMedicamentoResponse> listarTomasDelUsuario(String correo, LocalDateTime desde, LocalDateTime hasta);

    /**
     * Genera las tomas y recordatorios que falten en los próximos días para los horarios activos del usuario.
     * Se hace como máximo una vez al día por usuario. Complementa la tarea diaria, que en Cloud Run puede no
     * ejecutarse si el servicio está apagado por falta de tráfico.
     */
    void asegurarHorizonte(String correo);
    TomaMedicamentoResponse marcarNotificado(String correo, Long tomaId);
    TomaMedicamentoResponse marcarTomado(String correo, Long tomaId);

    /** Operación interna del planificador para ampliar el horizonte de tomas recurrentes. */
    void extenderHorizonteActivo();
}