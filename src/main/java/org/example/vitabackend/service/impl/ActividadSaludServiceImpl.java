package org.example.vitabackend.service.impl;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.response.ActividadSaludResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.ActividadSaludMapper;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.ActividadSaludService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementa las reglas de negocio para que cada usuario gestione exclusivamente sus actividades. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ActividadSaludServiceImpl implements ActividadSaludService {

    /** Zona del negocio: las fechas del calendario se interpretan como hora local de Colombia. */
    private static final ZoneId ZONA_VITA = ZoneId.of("America/Bogota");

    private final ActividadSaludRepository actividadSaludRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ActividadSaludMapper actividadSaludMapper;

    @Override
    @Transactional
    public ActividadSaludResponse crear(String correoAutenticado, ActividadSaludRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        validarFechaHoraFutura(request.fechaHora());
        ActividadSalud actividad = actividadSaludMapper.toEntity(request);
        actividad.setUsuario(usuario);
        actividad.setEstado(EstadoActividad.PENDIENTE);

        ActividadSalud guardada = actividadSaludRepository.save(actividad);
        return actividadSaludMapper.toResponse(guardada);
    }

    @Override
    public List<ActividadSaludResponse> listar(String correoAutenticado) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        return actividadSaludRepository.findByUsuario_IdOrderByFechaHoraAscIdAsc(usuario.getId())
                .stream()
                .map(actividadSaludMapper::toResponse)
                .toList();
    }

    @Override
    public List<ActividadSaludResponse> listarPorRango(
            String correoAutenticado, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new BusinessException("Debe indicar la fecha y hora inicial y final");
        }
        if (desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }

        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        return actividadSaludRepository
                .findByUsuario_IdAndFechaHoraBetweenOrderByFechaHoraAscIdAsc(usuario.getId(), desde, hasta)
                .stream()
                .map(actividadSaludMapper::toResponse)
                .toList();
    }

    /** Consulta todas las actividades del día local indicado. */
    @Override
    public List<ActividadSaludResponse> listarPorDia(String correoAutenticado, LocalDate fecha) {
        if (fecha == null) {
            throw new BusinessException("Debe indicar la fecha que desea consultar");
        }
        LocalDateTime desde = fecha.atStartOfDay();
        return listarPorPeriodo(correoAutenticado, desde, desde.plusDays(1));
    }

    /** Consulta la semana (lunes a lunes, fin exclusivo) que contiene la fecha. */
    @Override
    public List<ActividadSaludResponse> listarPorSemana(String correoAutenticado, LocalDate fecha) {
        if (fecha == null) {
            throw new BusinessException("Debe indicar la fecha que desea consultar");
        }
        LocalDate lunes = fecha.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime desde = lunes.atStartOfDay();
        return listarPorPeriodo(correoAutenticado, desde, desde.plusDays(7));
    }

    /** Consulta un mes calendario completo. mes debe estar entre 1 y 12. */
    @Override
    public List<ActividadSaludResponse> listarPorMes(String correoAutenticado, int anio, int mes) {
        final YearMonth yearMonth;
        try {
            yearMonth = YearMonth.of(anio, mes);
        } catch (DateTimeException ex) {
            throw new BusinessException("El año o el mes indicado no es válido");
        }
        LocalDateTime desde = yearMonth.atDay(1).atStartOfDay();
        return listarPorPeriodo(correoAutenticado, desde, desde.plusMonths(1));
    }

    private List<ActividadSaludResponse> listarPorPeriodo(
            String correoAutenticado, LocalDateTime desde, LocalDateTime hastaExclusivo) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        return actividadSaludRepository
                .findByUsuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                        usuario.getId(), desde, hastaExclusivo)
                .stream()
                .map(actividadSaludMapper::toResponse)
                .toList();
    }

    @Override
    public ActividadSaludResponse obtener(String correoAutenticado, Long actividadId) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        return actividadSaludMapper.toResponse(buscarActividadPropia(actividadId, usuario.getId()));
    }

    @Override
    @Transactional
    public ActividadSaludResponse actualizar(
            String correoAutenticado, Long actividadId, ActividadSaludRequest request) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        ActividadSalud actividad = buscarActividadPropia(actividadId, usuario.getId());

        validarFechaHoraFutura(request.fechaHora());

        if (actividad.getEstado() == EstadoActividad.CANCELADA) {
            throw new BusinessException("No se puede modificar una actividad cancelada");
        }

        LocalDateTime fechaHoraAnterior = actividad.getFechaHora();
        actividadSaludMapper.actualizarDesdeRequest(request, actividad);
        if (!fechaHoraAnterior.equals(actividad.getFechaHora())) {
            // Conserva la anticipación configurada: si la cita se mueve 90 minutos,
            // los recordatorios pendientes también se desplazan 90 minutos.
            java.time.Duration desplazamiento = java.time.Duration.between(
                    fechaHoraAnterior, actividad.getFechaHora());
            List<Recordatorio> recordatoriosFuturos = recordatorioRepository
                    .findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
                            actividad.getId(), EstadoRecordatorio.PENDIENTE, LocalDateTime.now(ZONA_VITA));
            if (!recordatoriosFuturos.isEmpty()) {
                recordatoriosFuturos.forEach(r -> r.setFechaHora(r.getFechaHora().plus(desplazamiento)));
                recordatorioRepository.saveAll(recordatoriosFuturos);
            }
        }
        return actividadSaludMapper.toResponse(actividadSaludRepository.save(actividad));
    }

    @Override
    @Transactional
    public ActividadSaludResponse completar(String correoAutenticado, Long actividadId) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        ActividadSalud actividad = buscarActividadPropia(actividadId, usuario.getId());

        if (actividad.getEstado() == EstadoActividad.CANCELADA) {
            throw new BusinessException("No se puede completar una actividad cancelada");
        }
        if (actividad.getEstado() != EstadoActividad.COMPLETADA) {
            actividad.setEstado(EstadoActividad.COMPLETADA);
            actividad.setFechaHoraCumplimiento(LocalDateTime.now(ZONA_VITA));;
            
            List<Recordatorio> recordatoriosFuturos = recordatorioRepository
                    .findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
                            actividad.getId(), EstadoRecordatorio.PENDIENTE, LocalDateTime.now(ZONA_VITA));
            if (!recordatoriosFuturos.isEmpty()) {
                recordatoriosFuturos.forEach(r -> r.setEstado(EstadoRecordatorio.CANCELADO));
                recordatorioRepository.saveAll(recordatoriosFuturos);
            }
            actividad = actividadSaludRepository.save(actividad);
        }
        return actividadSaludMapper.toResponse(actividad);
    }

    /**
     * Cancela lógicamente una actividad y sus recordatorios pendientes futuros.
     * No elimina físicamente datos ni modifica recordatorios ya enviados o confirmados.
     */
    @Override
    @Transactional
    public void cancelar(String correoAutenticado, Long actividadId) {
        Usuario usuario = obtenerUsuarioAutenticado(correoAutenticado);
        ActividadSalud actividad = buscarActividadPropia(actividadId, usuario.getId());

        if (actividad.getEstado() == EstadoActividad.COMPLETADA) {
            throw new BusinessException("No se puede cancelar una actividad completada");
        }
        if (actividad.getEstado() == EstadoActividad.CANCELADA) {
            return; // Operación idempotente: volver a cancelar no cambia el resultado.
        }

        actividad.setEstado(EstadoActividad.CANCELADA);
        actividadSaludRepository.save(actividad);

        List<Recordatorio> pendientesFuturos = recordatorioRepository
                .findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
                        actividad.getId(), EstadoRecordatorio.PENDIENTE, LocalDateTime.now(ZONA_VITA));
        if (!pendientesFuturos.isEmpty()) {
            pendientesFuturos.forEach(recordatorio -> recordatorio.setEstado(EstadoRecordatorio.CANCELADO));
            recordatorioRepository.saveAll(pendientesFuturos);
        }
    }

     /**
     * Las actividades del calendario representan compromisos futuros.
     * Se acepta únicamente una fecha/hora estrictamente posterior al instante actual.
     */
    private void validarFechaHoraFutura(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return; // @NotNull se encarga de la petición HTTP; protege también llamadas internas.
        }
        if (!fechaHora.isAfter(LocalDateTime.now(ZONA_VITA))) {
            throw new BusinessException("La fecha y hora de la actividad deben ser futuras");
        }
    }

    private Usuario obtenerUsuarioAutenticado(String correoAutenticado) {
        if (correoAutenticado == null || correoAutenticado.isBlank()) {
            throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        }
        return usuarioRepository.findByCorreo(correoAutenticado)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }

    private ActividadSalud buscarActividadPropia(Long actividadId, Long usuarioId) {
        return actividadSaludRepository.findByIdAndUsuario_Id(actividadId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Actividad de salud no encontrada"));
    }
}
