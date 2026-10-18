package org.example.vitabackend.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.RecordatorioRequest;
import org.example.vitabackend.dto.response.RecordatorioResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.RecordatorioMapper;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.RecordatorioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reglas de negocio para recordatorios de actividades; el móvil programa la notificación local. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecordatorioServiceImpl implements RecordatorioService {

    private static final ZoneId ZONA_VITA = ZoneId.of("America/Bogota");
    private final RecordatorioRepository recordatorioRepository;
    private final ActividadSaludRepository actividadRepository;
    private final UsuarioRepository usuarioRepository;
    private final RecordatorioMapper mapper;

    @Override
    @Transactional
    public RecordatorioResponse crearParaActividad(String correo, Long actividadId, RecordatorioRequest request) {
        Usuario usuario = obtenerUsuario(correo);
        ActividadSalud actividad = actividadRepository.findByIdAndUsuario_Id(actividadId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Actividad de salud no encontrada"));

        if (actividad.getEstado() != EstadoActividad.PENDIENTE) {
            throw new BusinessException("Solo se pueden programar recordatorios para actividades pendientes");
        }
        LocalDateTime ahora = LocalDateTime.now(ZONA_VITA);
        if (!request.fechaHora().isAfter(ahora)) {
            throw new BusinessException("El recordatorio debe programarse para una fecha y hora futura");
        }
        // Puede ser antes de la actividad o a la misma hora, pero no después
        if (request.fechaHora().isAfter(actividad.getFechaHora())) {
            throw new BusinessException("El recordatorio no puede programarse después de la actividad de salud");
        }

        Recordatorio recordatorio = Recordatorio.builder()
                .fechaHora(request.fechaHora())
                .mensaje(request.mensaje().trim())
                .tipoRecordatorio(request.tipoRecordatorio())
                .estado(EstadoRecordatorio.PENDIENTE)
                .actividadSalud(actividad)
                .tomaMedicamento(null)
                .build();
        return mapper.toResponse(recordatorioRepository.save(recordatorio));
    }

    @Override
    public List<RecordatorioResponse> listarDeActividad(String correo, Long actividadId) {
        Usuario usuario = obtenerUsuario(correo);
        actividadRepository.findByIdAndUsuario_Id(actividadId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Actividad de salud no encontrada"));
        return recordatorioRepository
                .findByActividadSalud_IdAndActividadSalud_Usuario_IdOrderByFechaHoraAscIdAsc(actividadId, usuario.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<RecordatorioResponse> listarAgenda(String correo, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !desde.isBefore(hasta)) {
            throw new BusinessException("El intervalo debe tener fecha inicial y final, y la inicial debe ser anterior a la final");
        }
        Usuario usuario = obtenerUsuario(correo);
        return recordatorioRepository.findAgendaByUsuarioIdAndIntervalo(usuario.getId(), desde, hasta)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public RecordatorioResponse cancelar(String correo, Long recordatorioId) {
        Recordatorio recordatorio = buscarPropio(correo, recordatorioId);
        if (recordatorio.getEstado() == EstadoRecordatorio.CANCELADO) {
            return mapper.toResponse(recordatorio);
        }
        if (recordatorio.getEstado() != EstadoRecordatorio.PENDIENTE) {
            throw new BusinessException("Solo se pueden cancelar recordatorios pendientes");
        }
        recordatorio.setEstado(EstadoRecordatorio.CANCELADO);
        return mapper.toResponse(recordatorioRepository.save(recordatorio));
    }

    /** El cliente puede informar que la notificación local apareció; no equivale a actividad completada. */
    @Override
    @Transactional
    public RecordatorioResponse marcarEnviado(String correo, Long recordatorioId) {
        Recordatorio recordatorio = buscarPropio(correo, recordatorioId);
        if (recordatorio.getEstado() == EstadoRecordatorio.CANCELADO) {
            throw new BusinessException("El recordatorio está cancelado");
        }
        if (recordatorio.getEstado() == EstadoRecordatorio.PENDIENTE) {
            recordatorio.setEstado(EstadoRecordatorio.ENVIADO);
            recordatorioRepository.save(recordatorio);
        }
        return mapper.toResponse(recordatorio);
    }

    /** CONFIRMADO significa que el usuario reconoció el recordatorio, no que la actividad esté completada. */
    @Override
    @Transactional
    public RecordatorioResponse confirmar(String correo, Long recordatorioId) {
        Recordatorio recordatorio = buscarPropio(correo, recordatorioId);
        if (recordatorio.getEstado() == EstadoRecordatorio.CANCELADO) {
            throw new BusinessException("El recordatorio está cancelado");
        }
        if (recordatorio.getEstado() != EstadoRecordatorio.CONFIRMADO) {
            recordatorio.setEstado(EstadoRecordatorio.CONFIRMADO);
            recordatorioRepository.save(recordatorio);
        }
        return mapper.toResponse(recordatorio);
    }

    private Recordatorio buscarPropio(String correo, Long recordatorioId) {
        Usuario usuario = obtenerUsuario(correo);
        return recordatorioRepository.findPropioById(recordatorioId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recordatorio no encontrado"));
    }

    private Usuario obtenerUsuario(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        }
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
}
