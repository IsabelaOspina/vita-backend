package org.example.vitabackend.service;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.dto.request.RecordatorioRequest;
import org.example.vitabackend.dto.response.RecordatorioResponse;

/** Casos de uso para los recordatorios propios del usuario autenticado. */
public interface RecordatorioService {
    RecordatorioResponse crearParaActividad(String correo, Long actividadId, RecordatorioRequest request);
    List<RecordatorioResponse> listarDeActividad(String correo, Long actividadId);
    List<RecordatorioResponse> listarAgenda(String correo, LocalDateTime desde, LocalDateTime hasta);
    RecordatorioResponse cancelar(String correo, Long recordatorioId);
    RecordatorioResponse marcarEnviado(String correo, Long recordatorioId);
    RecordatorioResponse confirmar(String correo, Long recordatorioId);
}
