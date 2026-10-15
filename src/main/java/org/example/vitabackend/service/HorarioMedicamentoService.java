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
    TomaMedicamentoResponse marcarNotificado(String correo, Long tomaId);
    TomaMedicamentoResponse marcarTomado(String correo, Long tomaId);

    /** Operación interna del planificador para ampliar el horizonte de tomas recurrentes. */
    void extenderHorizonteActivo();
}
