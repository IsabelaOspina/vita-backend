package org.example.vitabackend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.example.vitabackend.domain.enums.EstadoEmergencia;
import org.example.vitabackend.domain.enums.TipoEmergencia;
import org.example.vitabackend.domain.model.ContactoEmergencia;
import org.example.vitabackend.domain.model.SolicitudEmergencia;
import org.example.vitabackend.domain.model.Usuario;

import org.example.vitabackend.dto.request.SolicitudEmergenciaRequest;
import org.example.vitabackend.dto.response.SolicitudEmergenciaResponse;

import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;

import org.example.vitabackend.repository.SolicitudEmergenciaRepository;
import org.example.vitabackend.repository.UsuarioRepository;

import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.EmergenciaService;
import org.example.vitabackend.service.InalambriaSmsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmergenciaServiceImpl implements EmergenciaService {

    private static final Logger logger =
            LoggerFactory.getLogger(EmergenciaServiceImpl.class);
    private final SolicitudEmergenciaRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final InalambriaSmsService inalambriaSmsService;

    @Override
    @Transactional
    public SolicitudEmergenciaResponse solicitar(
            SolicitudEmergenciaRequest request
    ) {

        Usuario usuario = buscarUsuario();

        ContactoEmergencia contacto = usuario.getContactoEmergencia();

        if (contacto == null) {
            throw new BusinessException(
                    "Debes registrar un contacto de emergencia antes de solicitar asistencia"
            );
        }

        // Creamos primero la solicitud.
        SolicitudEmergencia solicitud = SolicitudEmergencia.builder()
                .fechaHora(LocalDateTime.now())
                .tipo(request.tipo())
                .ubicacion(limpiarUbicacion(request.ubicacion()))
                .estado(EstadoEmergencia.PENDIENTE)
                .usuario(usuario)
                .contactoEmergencia(contacto)
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Construimos el mensaje que recibirá el contacto.
        String mensaje = construirMensaje(usuario, solicitud);

        try {

            inalambriaSmsService.enviarSms(
                    telefonoInternacional(contacto.getTelefono()),
                    mensaje
            );

            logger.info("SMS enviado mediante Inalambria Express.");

            solicitud.setEstado(EstadoEmergencia.ENVIADA);

        } catch (Exception e) {

            logger.error("ERROR AL ENVIAR SMS CON INALAMBRIA", e);

            solicitud.setEstado(EstadoEmergencia.FALLIDA);
        }

        solicitud = solicitudRepository.save(solicitud);

        return convertirRespuesta(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudEmergenciaResponse> historial() {

        Long usuarioId = usuarioActual.id();

        return solicitudRepository
                .findByUsuarioIdOrderByFechaHoraDesc(usuarioId)
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SolicitudEmergenciaResponse obtener(Long id) {

        SolicitudEmergencia solicitud = buscarSolicitudPropia(id);

        return convertirRespuesta(solicitud);
    }

    @Override
    @Transactional
    public SolicitudEmergenciaResponse cancelar(Long id) {

        SolicitudEmergencia solicitud = buscarSolicitudPropia(id);

        if (solicitud.getEstado() != EstadoEmergencia.PENDIENTE) {
            throw new BusinessException(
                    "Solo se pueden cancelar solicitudes pendientes"
            );
        }

        solicitud.setEstado(EstadoEmergencia.CANCELADA);

        return convertirRespuesta(
                solicitudRepository.save(solicitud)
        );
    }

    @Override
    @Transactional
    public SolicitudEmergenciaResponse marcarAtendida(Long id) {

        SolicitudEmergencia solicitud = buscarSolicitudPropia(id);

        if (solicitud.getEstado() != EstadoEmergencia.ENVIADA) {
            throw new BusinessException(
                    "Solo se pueden marcar como atendidas las solicitudes enviadas"
            );
        }

        solicitud.setEstado(EstadoEmergencia.ATENDIDA);

        return convertirRespuesta(
                solicitudRepository.save(solicitud)
        );
    }

    private Usuario buscarUsuario() {

        return usuarioRepository.findById(usuarioActual.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"
                ));
    }

    private SolicitudEmergencia buscarSolicitudPropia(Long id) {

        Long usuarioId = usuarioActual.id();

        return solicitudRepository.findById(id)
                .filter(s -> s.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró la solicitud de emergencia"
                ));
    }

    private String construirMensaje(
            Usuario usuario,
            SolicitudEmergencia solicitud
    ) {

        StringBuilder mensaje = new StringBuilder();

        mensaje.append("ALERTA DE EMERGENCIA - VITA. ");
        mensaje.append(usuario.getNombre());
        mensaje.append(" ha solicitado asistencia. ");

        mensaje.append("Tipo: ");
        mensaje.append(describirTipo(solicitud.getTipo()));
        mensaje.append(". ");

        if (solicitud.getUbicacion() != null
                && !solicitud.getUbicacion().isBlank()) {

            mensaje.append("Ubicacion: ");
            mensaje.append(solicitud.getUbicacion());
            mensaje.append(". ");
        }

        mensaje.append("Por favor, comunicate con esta persona lo antes posible.");

        return mensaje.toString();
    }

    private String describirTipo(TipoEmergencia tipo) {

        return switch (tipo) {
            case ACCIDENTE -> "Accidente";
            case MEDICA -> "Emergencia medica";
            case OTRA -> "Otra emergencia";
        };
    }

    /**
     * El SMS exige el número en formato internacional (+57...)
     */
    static String telefonoInternacional(String telefono) {

        if (telefono == null) {
            return null;
        }

        String numero = telefono.replaceAll("[\\s-]", "");

        if (numero.startsWith("+")) {
            return numero;
        }

        if (numero.length() == 10) {
            return "+57" + numero;
        }

        if (numero.length() == 12 && numero.startsWith("57")) {
            return "+" + numero;
        }

        return numero;
    }

    private String limpiarUbicacion(String ubicacion) {

        if (ubicacion == null || ubicacion.isBlank()) {
            return null;
        }

        return ubicacion.trim();
    }

    private SolicitudEmergenciaResponse convertirRespuesta(
            SolicitudEmergencia solicitud
    ) {

        ContactoEmergencia contacto = solicitud.getContactoEmergencia();

        return new SolicitudEmergenciaResponse(
                solicitud.getId(),
                solicitud.getFechaHora(),
                solicitud.getTipo(),
                solicitud.getUbicacion(),
                solicitud.getEstado(),
                contacto != null ? contacto.getNombre() : null,
                contacto != null ? contacto.getTelefono() : null
        );
    }
}