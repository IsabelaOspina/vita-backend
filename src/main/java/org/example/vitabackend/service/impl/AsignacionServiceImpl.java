package org.example.vitabackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.model.AsignacionPaciente;
import org.example.vitabackend.domain.model.PersonalSalud;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.response.AsignacionResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.AsignacionPacienteRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.AsignacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsignacionServiceImpl implements AsignacionService {

    private final UsuarioRepository usuarioRepository;
    private final AsignacionPacienteRepository asignacionRepository;

    @Override
    @Transactional
    public AsignacionResponse asignar(Long profesionalId, Long pacienteId) {
        Usuario profesional = buscarProfesional(profesionalId);
        Usuario paciente = buscarPaciente(pacienteId);

        if (!Boolean.TRUE.equals(profesional.getActivo()) || !Boolean.TRUE.equals(paciente.getActivo())) {
            throw new BusinessException("Las dos cuentas deben estar activas para crear la asignación");
        }

        PersonalSalud personalSalud = profesional.getPersonalSalud();
        if (personalSalud == null) {
            throw new BusinessException("La cuenta del profesional no tiene datos de personal de salud");
        }

        if (asignacionRepository.existsByPersonalSalud_Usuario_IdAndPaciente_Id(profesionalId, pacienteId)) {
            throw new BusinessException("Ese paciente ya está asignado a ese profesional");
        }
        // Regla de negocio: varios profesionales por paciente, pero de distinta especialidad
        if (asignacionRepository.existsByPaciente_IdAndPersonalSalud_EspecialidadIgnoreCase(
                pacienteId, personalSalud.getEspecialidad())) {
            throw new BusinessException("El paciente ya tiene un profesional de la especialidad "
                    + personalSalud.getEspecialidad());
        }

        AsignacionPaciente asignacion = AsignacionPaciente.builder()
                .personalSalud(personalSalud)
                .paciente(paciente)
                .fechaAsignacion(LocalDateTime.now())
                .build();
        return aRespuesta(asignacionRepository.save(asignacion));
    }

    @Override
    @Transactional
    public void desasignar(Long profesionalId, Long pacienteId) {
        AsignacionPaciente asignacion = asignacionRepository
                .findByPersonalSalud_Usuario_IdAndPaciente_Id(profesionalId, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Ese paciente no está asignado a ese profesional"));
        asignacionRepository.delete(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponse> pacientesDe(Long profesionalId) {
        buscarProfesional(profesionalId);
        return asignacionRepository.findByPersonalSalud_Usuario_Id(profesionalId).stream()
                .map(this::aRespuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionResponse> profesionalesDe(Long pacienteId) {
        buscarPaciente(pacienteId);
        return asignacionRepository.findByPaciente_Id(pacienteId).stream()
                .map(this::aRespuesta).toList();
    }

    private Usuario buscarProfesional(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado: " + id));
        if (usuario.getRol() != Rol.PERSONAL_SALUD) {
            throw new BusinessException("La cuenta " + id + " no es de personal de salud");
        }
        return usuario;
    }

    private Usuario buscarPaciente(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));
        if (usuario.getRol() != Rol.USUARIO) {
            throw new BusinessException("La cuenta " + id + " no es un paciente");
        }
        return usuario;
    }

    private AsignacionResponse aRespuesta(AsignacionPaciente a) {
        PersonalSalud personalSalud = a.getPersonalSalud();
        Usuario profesional = personalSalud.getUsuario();
        Usuario paciente = a.getPaciente();
        return new AsignacionResponse(
                a.getId(),
                paciente.getId(),
                paciente.getNombre(),
                profesional.getId(),
                profesional.getNombre(),
                personalSalud.getEspecialidad(),
                personalSalud.getNumeroRegistroProfesional(),
                a.getFechaAsignacion());
    }
}
