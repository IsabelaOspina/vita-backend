package org.example.vitabackend.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.example.vitabackend.domain.enums.EstadoEmergencia;
import org.example.vitabackend.domain.enums.EstadoNotificacion;
import org.example.vitabackend.domain.enums.TipoEmergencia;
import org.example.vitabackend.domain.model.ContactoEmergencia;
import org.example.vitabackend.domain.model.Notificacion;
import org.example.vitabackend.domain.model.SolicitudEmergencia;
import org.example.vitabackend.domain.model.Usuario;

import org.example.vitabackend.dto.request.SolicitudEmergenciaRequest;
import org.example.vitabackend.dto.response.SolicitudEmergenciaResponse;

import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;

import org.example.vitabackend.repository.NotificacionRepository;
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
    private static final String URL_MAPA = "https://maps.google.com/?q=";
    private final SolicitudEmergenciaRepository solicitudRepository;
    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioActual usuarioActual;
    private final InalambriaSmsService inalambriaSmsService;

    @Override
    @Transactional
    public SolicitudEmergenciaResponse solicitar(
            SolicitudEmergenciaRequest request
    ) {
        // Comprobamos que las coordenadas estén completas y dentro de rango
        validarCoordenadas(request.latitud(), request.longitud());

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
                .latitud(request.latitud())
                .longitud(request.longitud())
                .estado(EstadoEmergencia.PENDIENTE)
                .usuario(usuario)
                .contactoEmergencia(contacto)
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Construimos el mensaje que recibirá el contacto.
        String mensaje = construirMensaje(usuario, solicitud);

        // Registramos la notificación
        Notificacion notificacion = Notificacion.builder()
                .fechaHora(LocalDateTime.now())
                .mensaje(mensaje)
                .estado(EstadoNotificacion.PENDIENTE)
                .solicitudEmergencia(solicitud)
                .contactoEmergencia(contacto)
                .build();
        
        notificacion = notificacionRepository.save(notificacion);

        // Realizar el envió mediante Inalambria
        try {

            inalambriaSmsService.enviarSms(
                    telefonoInternacional(contacto.getTelefono()),
                    mensaje
            );

            // El proveedor aceptó la solicitud de envió
            solicitud.setEstado(EstadoEmergencia.ENVIADA);
            notificacion.setEstado(EstadoNotificacion.ENVIADA);

            logger.info("Solicitud de emergencia {} aceptada para envío SMS.", solicitud.getId());

        } catch (Exception e) {

            // Se conserva el registro aunque falle el envío
            solicitud.setEstado(EstadoEmergencia.FALLIDA);
            notificacion.setEstado(EstadoNotificacion.FALLIDA);  

            logger.error("Falló el envío SMS de la solicitud {}.", solicitud.getId(), e);
            
        }

        // Actualizamos ambos registros con el resultado final
        solicitud = solicitudRepository.save(solicitud);
        notificacionRepository.save(notificacion);

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

    private void validarCoordenadas(BigDecimal latitud, BigDecimal longitud) {
        // Las dos coordenadas deben enviarse juntas o ambas deben ser nulas
        if((latitud == null) != (longitud == null)) {
            throw new BusinessException("Debe proporcionar tanto la latitud como la longitud");
        }

        if(latitud == null) {
            return;
        }

        if(latitud.compareTo(BigDecimal.valueOf(-90)) < 0 || latitud.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BusinessException("La latitud debe estar entre -90 y 90");
        } 

        if(longitud.compareTo(BigDecimal.valueOf(-180)) < 0 || longitud.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BusinessException("La latitud debe estar entre -180 y 180");
        } 
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

        // Ubicación automática o manual
        if (solicitud.getUbicacion() != null
                && !solicitud.getUbicacion().isBlank()) {

            mensaje.append("Ubicacion: ");
            mensaje.append(solicitud.getUbicacion());
            mensaje.append(". ");
        }

        // Enlace directo al mapa cuando existen coordenadas
        if (solicitud.getLatitud() != null && solicitud.getLongitud() != null) {

            mensaje.append("Mapa: ");
            mensaje.append(URL_MAPA);
            mensaje.append(solicitud.getLatitud().toPlainString());
            mensaje.append(",");
            mensaje.append(solicitud.getLongitud().toPlainString());
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
                contacto != null ? contacto.getTelefono() : null,
                solicitud.getLatitud(),
                solicitud.getLongitud()
        );
    }
}