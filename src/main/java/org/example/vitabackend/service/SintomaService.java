package org.example.vitabackend.service;

import java.time.LocalDate;
import java.util.List;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.OrdenSintoma;
import org.example.vitabackend.dto.request.SintomaActualizarRequest;
import org.example.vitabackend.dto.request.SintomaFiltro;
import org.example.vitabackend.dto.request.SintomaRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;
import org.example.vitabackend.dto.response.TipoSintomaResponse;

/**
 * Seguimiento de síntomas. Todos los métodos reciben el id del PACIENTE dueño de los datos:
 * el controlador del paciente envía su propio id (sacado del token) y el del profesional
 * envía el del paciente asignado, tras verificar la asignación.
 */
public interface SintomaService {

    SintomaResponse registrar(Long pacienteId, SintomaRequest request);

    SintomaResponse obtener(Long pacienteId, Long sintomaId);

    SintomaResponse actualizar(Long pacienteId, Long sintomaId, SintomaActualizarRequest request);

    void eliminar(Long pacienteId, Long sintomaId);

    /** Historial paginado con filtros opcionales (rango de fechas, tipo, intensidad). */
    PaginaResponse<SintomaResponse> listar(Long pacienteId, SintomaFiltro filtro, OrdenSintoma orden,
                                           int pagina, int tamano);

    /** Indicadores del periodo. */
    SintomaResumenResponse resumen(Long pacienteId, LocalDate desde, LocalDate hasta);

    /** Evolución por día/semana/mes. */
    SintomaEvolucionResponse evolucion(Long pacienteId, String tipo, LocalDate desde, LocalDate hasta,
                                       AgrupacionTiempo agrupacion);

    /** Tipos de síntoma que el paciente ya registró, del más frecuente al menos frecuente. */
    List<TipoSintomaResponse> tiposRegistrados(Long pacienteId);
}
