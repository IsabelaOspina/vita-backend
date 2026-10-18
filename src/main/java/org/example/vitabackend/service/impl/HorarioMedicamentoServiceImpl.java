package org.example.vitabackend.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.HorarioMedicamentoRequest;
import org.example.vitabackend.dto.response.HorarioMedicamentoResponse;
import org.example.vitabackend.dto.response.TomaMedicamentoResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.HorarioMedicamentoRepository;
import org.example.vitabackend.repository.MedicamentoRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.TomaMedicamentoRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HorarioMedicamentoServiceImpl implements HorarioMedicamentoService {
    private static final ZoneId ZONA_VITA = ZoneId.of("America/Bogota");
    private static final int HORIZONTE_DIAS = 30;
    private final HorarioMedicamentoRepository horarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final TomaMedicamentoRepository tomaRepository;
    private final RecordatorioRepository recordatorioRepository;
    private final UsuarioRepository usuarioRepository;
    private final TomaMedicamentoOccurrenceCalculator calculator;

    /** Último día en que se revisó el horizonte de tomas de cada usuario (ver asegurarHorizonte). */
    private final Map<Long, LocalDate> horizonteRevisado = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public HorarioMedicamentoResponse crear(String correo, Long medicamentoId, HorarioMedicamentoRequest request) {
        Usuario usuario = usuario(correo);
        Medicamento medicamento = medicamentoRepository.findByIdAndUsuario_Id(medicamentoId, usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));
        if (!Boolean.TRUE.equals(medicamento.getActivo())) throw new BusinessException("El medicamento está inactivo");
        validar(request);

        Set<java.time.DayOfWeek> dias = request.diasSemana() == null ? new HashSet<>() : new HashSet<>(request.diasSemana());
        HorarioMedicamento horario = HorarioMedicamento.builder().medicamento(medicamento)
                .tipoFrecuencia(request.tipoFrecuencia()).fechaHoraInicio(request.fechaHoraInicio())
                .fechaHoraFin(request.fechaHoraFin()).intervaloHoras(request.intervaloHoras())
                .horaProgramada(request.horaProgramada()).diasSemana(dias)
                .tipoRecordatorio(request.tipoRecordatorio()).activo(true).build();
        horario = horarioRepository.save(horario);

        LocalDateTime ahora = LocalDateTime.now(ZONA_VITA).withNano(0);
        generarOcurrencias(horario, ahora, ahora.plusDays(HORIZONTE_DIAS));
        return response(horario);
    }

    @Override
    public List<HorarioMedicamentoResponse> listar(String correo, Long medicamentoId) {
        Usuario u = usuario(correo);
        medicamentoRepository.findByIdAndUsuario_Id(medicamentoId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));
        return horarioRepository.findByMedicamento_IdAndMedicamento_Usuario_IdOrderByIdAsc(medicamentoId, u.getId())
                .stream().map(this::response).toList();
    }

    @Override
    @Transactional
    public void cancelar(String correo, Long medicamentoId, Long horarioId) {
        Usuario u = usuario(correo);
        HorarioMedicamento horario = horarioRepository
                .findByIdAndMedicamento_IdAndMedicamento_Usuario_Id(horarioId, medicamentoId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Horario de medicamento no encontrado"));
        if (!Boolean.TRUE.equals(horario.getActivo())) return;
        horario.setActivo(false);
        horarioRepository.save(horario);
        LocalDateTime ahora = LocalDateTime.now(ZONA_VITA);
        List<TomaMedicamento> futuras = tomaRepository.findByHorarioMedicamento_IdAndFechaHoraAfterAndEstadoIn(
                horarioId, ahora, List.of(EstadoTomaMed.PENDIENTE, EstadoTomaMed.NOTIFICADO));
        for (TomaMedicamento toma : futuras) {
            toma.setEstado(EstadoTomaMed.CANCELADO);
            tomaRepository.save(toma);
            List<Recordatorio> recordatorios = recordatorioRepository.findByTomaMedicamento_Id(toma.getId());
            recordatorios.stream().filter(r -> r.getEstado() == EstadoRecordatorio.PENDIENTE)
                    .forEach(r -> r.setEstado(EstadoRecordatorio.CANCELADO));
            recordatorioRepository.saveAll(recordatorios);
        }
    }

    @Override
    public List<TomaMedicamentoResponse> listarTomas(String correo, Long medicamentoId, Long horarioId,
                                                     LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !desde.isBefore(hasta)) {
            throw new BusinessException("El intervalo debe ser válido y la fecha inicial debe ser anterior a la final");
        }
        Usuario u = usuario(correo);
        medicamentoRepository.findByIdAndUsuario_Id(medicamentoId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));
        horarioRepository.findByIdAndMedicamento_IdAndMedicamento_Usuario_Id(horarioId, medicamentoId, u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Horario de medicamento no encontrado"));
        return tomaRepository.findByHorarioMedicamento_IdAndMedicamento_IdAndMedicamento_Usuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
                horarioId, medicamentoId, u.getId(), desde, hasta).stream().map(this::response).toList();
    }

    @Override
    public List<TomaMedicamentoResponse> listarTomasDelUsuario(String correo, LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null || !desde.isBefore(hasta)) {
            throw new BusinessException("El intervalo debe ser válido y la fecha inicial debe ser anterior a la final");
        }
        Usuario u = usuario(correo);
        return tomaRepository.findDelUsuarioEntre(u.getId(), desde, hasta).stream().map(this::response).toList();
    }

    @Override
    @Transactional
    public void asegurarHorizonte(String correo) {
        Usuario u = usuario(correo);
        LocalDate hoy = LocalDate.now(ZONA_VITA);
        if (hoy.equals(horizonteRevisado.get(u.getId()))) return;
        LocalDateTime desde = LocalDateTime.now(ZONA_VITA).withNano(0);
        LocalDateTime hasta = desde.plusDays(HORIZONTE_DIAS);
        for (HorarioMedicamento horario : horarioRepository.findByActivoTrueAndMedicamento_Usuario_Id(u.getId())) {
            generarOcurrencias(horario, desde, hasta);
        }
        horizonteRevisado.put(u.getId(), hoy);
    }

    @Override
    @Transactional
    public TomaMedicamentoResponse marcarNotificado(String correo, Long tomaId) {
        Usuario u = usuario(correo);
        TomaMedicamento toma = buscarTomaPropia(tomaId, u.getId());
        if (toma.getEstado() == EstadoTomaMed.CANCELADO || toma.getEstado() == EstadoTomaMed.OMITIDO) {
            throw new BusinessException("No se puede notificar una toma cancelada u omitida");
        }
        if (toma.getEstado() == EstadoTomaMed.PENDIENTE) {
            toma.setEstado(EstadoTomaMed.NOTIFICADO);
            tomaRepository.save(toma);
            actualizarRecordatorioDeToma(toma, EstadoRecordatorio.ENVIADO);
        }
        return response(toma);
    }

    @Override
    @Transactional
    public TomaMedicamentoResponse marcarTomado(String correo, Long tomaId) {
        Usuario u = usuario(correo);
        TomaMedicamento toma = buscarTomaPropia(tomaId, u.getId());
        if (toma.getEstado() == EstadoTomaMed.CANCELADO || toma.getEstado() == EstadoTomaMed.OMITIDO) {
            throw new BusinessException("No se puede confirmar como tomada una dosis cancelada u omitida");
        }
        if (toma.getEstado() != EstadoTomaMed.TOMADO) {
            toma.setEstado(EstadoTomaMed.TOMADO);
            tomaRepository.save(toma);
            actualizarRecordatorioDeToma(toma, EstadoRecordatorio.CONFIRMADO);
        }
        return response(toma);
    }

    /** Puede invocarse desde una tarea diaria: crea el horizonte faltante sin duplicar tomas. */
    @Override
    @Transactional
    public void extenderHorizonteActivo() {
        LocalDateTime desde = LocalDateTime.now(ZONA_VITA).withNano(0);
        LocalDateTime hasta = desde.plusDays(HORIZONTE_DIAS);
        for (HorarioMedicamento horario : horarioRepository.findByActivoTrue()) {
            if (Boolean.TRUE.equals(horario.getMedicamento().getActivo())) generarOcurrencias(horario, desde, hasta);
        }
    }

    @Transactional
    public void generarOcurrencias(HorarioMedicamento horario, LocalDateTime desde, LocalDateTime hasta) {
        if (!Boolean.TRUE.equals(horario.getActivo()) || !Boolean.TRUE.equals(horario.getMedicamento().getActivo())) return;
        // Las fechas que ya tienen toma se traen en una sola consulta, en vez de preguntar una por una
        Set<LocalDateTime> existentes = new HashSet<>(tomaRepository.findFechasDeHorarioEntre(horario.getId(), desde, hasta));
        for (LocalDateTime fecha : calculator.calcular(horario, desde, hasta)) {
            if (existentes.contains(fecha)) continue;
            TomaMedicamento toma = TomaMedicamento.builder().medicamento(horario.getMedicamento())
                    .horarioMedicamento(horario).fechaHora(fecha).estado(EstadoTomaMed.PENDIENTE).build();
            toma = tomaRepository.save(toma);
            String dosis = horario.getMedicamento().getDosis();
            String mensaje = "Es hora de tomar " + horario.getMedicamento().getNombre()
                    + (dosis == null || dosis.isBlank() ? "" : ", dosis: " + dosis);
            recordatorioRepository.save(Recordatorio.builder().fechaHora(fecha).mensaje(mensaje)
                    .tipoRecordatorio(horario.getTipoRecordatorio()).estado(EstadoRecordatorio.PENDIENTE)
                    .actividadSalud(null).tomaMedicamento(toma).build());
        }
    }

    private void validar(HorarioMedicamentoRequest r) {
        if (r.fechaHoraFin() != null && !r.fechaHoraFin().isAfter(r.fechaHoraInicio())) {
            throw new BusinessException("La fecha final debe ser posterior a la fecha inicial");
        }
        switch (r.tipoFrecuencia()) {
            case CADA_X_HORAS -> {
                if (r.intervaloHoras() == null || r.intervaloHoras() < 1 || r.intervaloHoras() > 168) {
                    throw new BusinessException("Para CADA_X_HORAS indique un intervalo entre 1 y 168 horas");
                }
            }
            case DIARIO -> {
                if (r.horaProgramada() == null) throw new BusinessException("Para DIARIO debe indicar la hora programada");
            }
            case DIAS_SEMANA -> {
                if (r.horaProgramada() == null) throw new BusinessException("Para DIAS_SEMANA debe indicar la hora programada");
                if (r.diasSemana() == null || r.diasSemana().isEmpty()) {
                    throw new BusinessException("Seleccione al menos un día de la semana");
                }
            }
        }
    }

    private void actualizarRecordatorioDeToma(TomaMedicamento toma, EstadoRecordatorio estado) {
        for (Recordatorio recordatorio : recordatorioRepository.findByTomaMedicamento_Id(toma.getId())) {
            if (recordatorio.getEstado() != EstadoRecordatorio.CANCELADO) {
                recordatorio.setEstado(estado);
                recordatorioRepository.save(recordatorio);
            }
        }
    }

    private TomaMedicamento buscarTomaPropia(Long id, Long usuarioId) {
        return tomaRepository.findByIdAndMedicamento_Usuario_Id(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Toma de medicamento no encontrada"));
    }
    private Usuario usuario(String correo) {
        if (correo == null || correo.isBlank()) throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
    private HorarioMedicamentoResponse response(HorarioMedicamento h) {
        return new HorarioMedicamentoResponse(h.getId(), h.getMedicamento().getId(), h.getTipoFrecuencia(),
                h.getFechaHoraInicio(), h.getFechaHoraFin(), h.getIntervaloHoras(), h.getHoraProgramada(),
                h.getDiasSemana() == null ? Set.of() : Set.copyOf(h.getDiasSemana()), h.getTipoRecordatorio(), h.getActivo());
    }
    private TomaMedicamentoResponse response(TomaMedicamento t) {
        return new TomaMedicamentoResponse(t.getId(), t.getMedicamento().getId(), t.getMedicamento().getNombre(),
                t.getMedicamento().getDosis(), t.getFechaHora(), t.getEstado());
    }
}
