package org.example.vitabackend.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.MedicamentoRequest;
import org.example.vitabackend.dto.response.MedicamentoResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.HorarioMedicamentoRepository;
import org.example.vitabackend.repository.MedicamentoRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.TomaMedicamentoRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.MedicamentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicamentoServiceImpl implements MedicamentoService {
    private static final ZoneId ZONA_VITA = ZoneId.of("America/Bogota");
    private final MedicamentoRepository medicamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HorarioMedicamentoRepository horarioRepository;
    private final TomaMedicamentoRepository tomaRepository;
    private final RecordatorioRepository recordatorioRepository;

    @Override
    @Transactional
    public MedicamentoResponse crear(String correo, MedicamentoRequest request) {
        Usuario usuario = usuario(correo);
        Medicamento medicamento = Medicamento.builder().nombre(request.nombre().trim())
                .dosis(limpiar(request.dosis()))
                .viaAdministracion(limpiar(request.viaAdministracion())).usuario(usuario).activo(true).build();
        return response(medicamentoRepository.save(medicamento));
    }

    @Override
    public List<MedicamentoResponse> listar(String correo) {
        Usuario u = usuario(correo);
        return medicamentoRepository.findByUsuario_IdAndActivoTrueOrderByNombreAsc(u.getId())
                .stream().map(this::response).toList();
    }

    @Override
    public MedicamentoResponse obtener(String correo, Long id) {
        Usuario u = usuario(correo);
        return response(buscarPropio(id, u.getId()));
    }

    @Override
    @Transactional
    public MedicamentoResponse actualizar(String correo, Long id, MedicamentoRequest request) {
        Usuario u = usuario(correo);
        Medicamento m = buscarPropio(id, u.getId());
        if (!Boolean.TRUE.equals(m.getActivo())) throw new BusinessException("El medicamento está inactivo");
        m.setNombre(request.nombre().trim());
        m.setDosis(limpiar(request.dosis()));
        m.setViaAdministracion(limpiar(request.viaAdministracion()));
        return response(medicamentoRepository.save(m));
    }

    @Override
    @Transactional
    public void desactivar(String correo, Long id) {
        Usuario u = usuario(correo);
        Medicamento m = buscarPropio(id, u.getId());
        if (!Boolean.TRUE.equals(m.getActivo())) return;
        m.setActivo(false);
        medicamentoRepository.save(m);
        LocalDateTime ahora = LocalDateTime.now(ZONA_VITA);
        for (HorarioMedicamento horario : horarioRepository.findByMedicamento_IdAndMedicamento_Usuario_IdOrderByIdAsc(id, u.getId())) {
            horario.setActivo(false);
            horarioRepository.save(horario);
            List<TomaMedicamento> futuras = tomaRepository.findByHorarioMedicamento_IdAndFechaHoraAfterAndEstadoIn(
                    horario.getId(), ahora, List.of(EstadoTomaMed.PENDIENTE, EstadoTomaMed.NOTIFICADO));
            for (TomaMedicamento toma : futuras) {
                toma.setEstado(EstadoTomaMed.CANCELADO);
                tomaRepository.save(toma);
                List<Recordatorio> avisos = recordatorioRepository.findByTomaMedicamento_Id(toma.getId());
                for (Recordatorio aviso : avisos) {
                    if (aviso.getEstado() == EstadoRecordatorio.PENDIENTE) aviso.setEstado(EstadoRecordatorio.CANCELADO);
                }
                recordatorioRepository.saveAll(avisos);
            }
        }
    }

    private Medicamento buscarPropio(Long id, Long usuarioId) {
        return medicamentoRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado"));
    }
    private Usuario usuario(String correo) {
        if (correo == null || correo.isBlank()) throw new ResourceNotFoundException("Usuario autenticado no encontrado");
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado"));
    }
    private String limpiar(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private MedicamentoResponse response(Medicamento m) {
        return new MedicamentoResponse(m.getId(), m.getNombre(), m.getDosis(),
                m.getViaAdministracion(), m.getActivo());
    }
}
