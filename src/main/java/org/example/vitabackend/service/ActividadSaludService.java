package org.example.vitabackend.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.response.ActividadSaludResponse;

/** Casos de uso del calendario. El correo corresponde a la identidad autenticada por Spring Security. */
public interface ActividadSaludService {

    ActividadSaludResponse crear(String correoAutenticado, ActividadSaludRequest request);

    List<ActividadSaludResponse> listar(String correoAutenticado);

    List<ActividadSaludResponse> listarPorRango(String correoAutenticado, LocalDateTime desde, LocalDateTime hasta);

    List<ActividadSaludResponse> listarPorDia(String correoAutenticado, LocalDate fecha);

    List<ActividadSaludResponse> listarPorSemana(String correoAutenticado, LocalDate fecha);

    List<ActividadSaludResponse> listarPorMes(String correoAutenticado, int anio, int mes);

    ActividadSaludResponse obtener(String correoAutenticado, Long actividadId);

    ActividadSaludResponse actualizar(String correoAutenticado, Long actividadId, ActividadSaludRequest request);

    ActividadSaludResponse completar(String correoAutenticado, Long actividadId);

    void cancelar(String correoAutenticado, Long actividadId);
}
