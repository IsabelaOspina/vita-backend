package org.example.vitabackend.service.impl;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.config.Reloj;
import org.example.vitabackend.domain.model.Recomendacion;
import org.example.vitabackend.domain.model.Sintoma;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.RecomendacionRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.RecomendacionResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.RecomendacionMapper;
import org.example.vitabackend.repository.RecomendacionRepository;
import org.example.vitabackend.repository.SintomaRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.AccesoPaciente;
import org.example.vitabackend.service.RecomendacionService;
import org.example.vitabackend.service.SintomaAnalisis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecomendacionServiceImpl implements RecomendacionService {

    private static final int TAMANO_MAXIMO = 100;

    private final RecomendacionRepository recomendacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SintomaRepository sintomaRepository;
    private final AccesoPaciente accesoPaciente;
    private final Reloj reloj;

    @Override
    @Transactional
    public RecomendacionResponse crear(Long profesionalId, Long pacienteId, RecomendacionRequest request) {
        accesoPaciente.verificarAsignado(profesionalId, pacienteId);

        Usuario profesional = usuarioRepository.findById(profesionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Usuario paciente = usuarioRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        String mensaje = request.mensaje() == null ? "" : request.mensaje().trim();
        if (mensaje.isEmpty()) {
            throw new BusinessException("El mensaje de la recomendación es obligatorio");
        }

        Long sintomaId = null;
        String referencia = null;
        if (request.sintomaId() != null) {
            Sintoma sintoma = sintomaRepository.findByIdAndUsuario_Id(request.sintomaId(), pacienteId)
                    .orElseThrow(() -> new BusinessException("El síntoma indicado no pertenece a ese paciente"));
            sintomaId = sintoma.getId();
            referencia = sintoma.getTipo() + ", intensidad " + sintoma.getIntensidad() + " de 10, "
                    + SintomaAnalisis.fechaLegible(sintoma.getFechaHora().toLocalDate());
        }

        Recomendacion recomendacion = Recomendacion.builder()
                .paciente(paciente)
                .profesional(profesional)
                .mensaje(mensaje)
                .sintomaId(sintomaId)
                .sintomaReferencia(referencia)
                .fechaCreacion(reloj.ahora())
                .leida(false)
                .build();
        return RecomendacionMapper.aRespuesta(recomendacionRepository.save(recomendacion));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<RecomendacionResponse> listarDelProfesional(Long profesionalId, Long pacienteId,
                                                                      int pagina, int tamano) {
        accesoPaciente.verificarAsignado(profesionalId, pacienteId);
        return aPagina(recomendacionRepository.findByPaciente_IdAndProfesional_Id(
                pacienteId, profesionalId, paginar(pagina, tamano)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginaResponse<RecomendacionResponse> listarDelPaciente(Long pacienteId, boolean soloNoLeidas,
                                                                   int pagina, int tamano) {
        Pageable pageable = paginar(pagina, tamano);
        return aPagina(soloNoLeidas
                ? recomendacionRepository.findByPaciente_IdAndLeidaFalse(pacienteId, pageable)
                : recomendacionRepository.findByPaciente_Id(pacienteId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarNoLeidas(Long pacienteId) {
        return recomendacionRepository.countByPaciente_IdAndLeidaFalse(pacienteId);
    }

    @Override
    @Transactional
    public RecomendacionResponse marcarLeida(Long pacienteId, Long recomendacionId) {
        // 404 también si es de otro paciente: no se revela que existe
        Recomendacion recomendacion = recomendacionRepository.findByIdAndPaciente_Id(recomendacionId, pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Recomendación no encontrada"));
        if (!Boolean.TRUE.equals(recomendacion.getLeida())) {
            recomendacion.setLeida(true);
            LocalDateTime ahora = reloj.ahora();
            recomendacion.setFechaLectura(ahora);
        }
        return RecomendacionMapper.aRespuesta(recomendacion);
    }

    // ---------- ayudas privadas ----------

    private Pageable paginar(int pagina, int tamano) {
        if (pagina < 0) {
            throw new BusinessException("La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new BusinessException("El tamaño de página debe estar entre 1 y " + TAMANO_MAXIMO);
        }
        return PageRequest.of(pagina, tamano, Sort.by(Sort.Order.desc("fechaCreacion"), Sort.Order.desc("id")));
    }

    private PaginaResponse<RecomendacionResponse> aPagina(Page<Recomendacion> resultado) {
        return new PaginaResponse<>(
                resultado.getContent().stream().map(RecomendacionMapper::aRespuesta).toList(),
                resultado.getNumber(), resultado.getSize(), resultado.getTotalElements(),
                resultado.getTotalPages(), resultado.isLast());
    }
}
