package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import org.example.vitabackend.domain.enums.EstadoEmergencia;
import org.example.vitabackend.domain.enums.TipoEmergencia;
import org.example.vitabackend.domain.model.ContactoEmergencia;
import org.example.vitabackend.domain.model.SolicitudEmergencia;
import org.example.vitabackend.domain.model.Notificacion;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.SolicitudEmergenciaRequest;
import org.example.vitabackend.dto.response.SolicitudEmergenciaResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.SolicitudEmergenciaRepository;
import org.example.vitabackend.repository.NotificacionRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.InalambriaSmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmergenciaServiceImplTest {

    private static final Long USUARIO_ID = 1L;
    private static final Long OTRO_USUARIO_ID = 2L;
    private static final Long SOLICITUD_ID = 10L;

    @Mock
    private SolicitudEmergenciaRepository solicitudRepository;

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioActual usuarioActual;

    @Mock
    private InalambriaSmsService inalambriaSmsService;

    @InjectMocks
    private EmergenciaServiceImpl emergenciaService;

    private Usuario usuario;
    private ContactoEmergencia contacto;

    @BeforeEach
    void configurar() {
        contacto = ContactoEmergencia.builder()
                .id(1L)
                .nombre("María")
                .telefono("+573001234567")
                .parentesco("Madre")
                .build();

        usuario = Usuario.builder()
                .id(USUARIO_ID)
                .nombre("Carlos")
                .correo("carlos@vita.com")
                .contactoEmergencia(contacto)
                .build();

        // lenient: la prueba de telefonoInternacional no usa el usuario actual
        lenient().when(usuarioActual.id()).thenReturn(USUARIO_ID);
    }

    private SolicitudEmergencia solicitud(EstadoEmergencia estado) {
        return SolicitudEmergencia.builder()
                .id(SOLICITUD_ID)
                .tipo(TipoEmergencia.MEDICA)
                .ubicacion("Armenia, Quindío")
                .estado(estado)
                .usuario(usuario)
                .contactoEmergencia(contacto)
                .build();
    }

    @Test
    void solicitar_ConDatosValidosGuardaYEnviaLaEmergencia() {
        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario));

        when(solicitudRepository.save(any(SolicitudEmergencia.class)))
                .thenAnswer(invocacion -> {
                    SolicitudEmergencia solicitud = invocacion.getArgument(0);
                    solicitud.setId(SOLICITUD_ID);
                    return solicitud;
                });
        
        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudEmergenciaResponse respuesta = emergenciaService.solicitar(
                new SolicitudEmergenciaRequest(
                        TipoEmergencia.MEDICA,
                        "Armenia, Quindío",
                        new BigDecimal("4.5339"),
                        new BigDecimal("-75.6811")
                )
        );

        assertEquals(EstadoEmergencia.ENVIADA, respuesta.estado());
        assertEquals(TipoEmergencia.MEDICA, respuesta.tipo());
        assertEquals("Armenia, Quindío", respuesta.ubicacion());
        assertEquals("María", respuesta.nombreContacto());
        assertEquals("+573001234567", respuesta.telefonoContacto());

        verify(inalambriaSmsService).enviarSms(
                eq("+573001234567"),
                contains("Armenia, Quindío")
        );

        verify(solicitudRepository, times(2))
                .save(any(SolicitudEmergencia.class));

        verify(notificacionRepository, times(2))
                .save(any(Notificacion.class));
    }

    @Test
    void solicitar_SinContactoEmergenciaLanzaExcepcion() {
        usuario.setContactoEmergencia(null);

        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario));

        assertThrows(BusinessException.class, () ->
                emergenciaService.solicitar(
                        new SolicitudEmergenciaRequest(
                                TipoEmergencia.MEDICA, 
                                null,
                                null,
                                null
                        )
                )
        );

        verify(solicitudRepository, never()).save(any());
        verify(inalambriaSmsService, never()).enviarSms(anyString(), anyString());
    }

    @Test
    void solicitar_CuandoFallaElSmsGuardaLaSolicitudComoFallida() {
        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario));

        when(solicitudRepository.save(any(SolicitudEmergencia.class)))
                .thenAnswer(invocacion -> {
                    SolicitudEmergencia solicitud = invocacion.getArgument(0);
                    solicitud.setId(SOLICITUD_ID);
                    return solicitud;
                });
        
        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        doThrow(new RuntimeException("Error de Inalambria"))
                .when(inalambriaSmsService)
                .enviarSms(anyString(), anyString());

        SolicitudEmergenciaResponse respuesta = emergenciaService.solicitar(
                new SolicitudEmergenciaRequest(
                        TipoEmergencia.MEDICA, 
                        null,
                        null,
                        null
                )
        );

        assertEquals(EstadoEmergencia.FALLIDA, respuesta.estado());

        verify(solicitudRepository, times(2)).save(any(SolicitudEmergencia.class));
        verify(notificacionRepository, times(2)).save(any(Notificacion.class));
    }

    @Test
    void solicitar_EliminaEspaciosSobrantesDeLaUbicacion() {
        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario));

        when(solicitudRepository.save(any(SolicitudEmergencia.class)))
                .thenAnswer(invocacion -> {
                    SolicitudEmergencia solicitud = invocacion.getArgument(0);
                    solicitud.setId(SOLICITUD_ID);
                    return solicitud;
                });
        
        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudEmergenciaResponse respuesta = emergenciaService.solicitar(
                new SolicitudEmergenciaRequest(
                        TipoEmergencia.ACCIDENTE,
                        "  Armenia, Quindío  ",
                        new BigDecimal("4.5339"),
                        new BigDecimal("-75.6811")
                )
        );

        assertEquals("Armenia, Quindío", respuesta.ubicacion());
    }

    @Test
    void solicitar_SinUbicacionLaGuardaComoNull() {
        when(usuarioRepository.findById(USUARIO_ID))
                .thenReturn(Optional.of(usuario));

        when(solicitudRepository.save(any(SolicitudEmergencia.class)))
                .thenAnswer(invocacion -> {
                    SolicitudEmergencia solicitud = invocacion.getArgument(0);
                    solicitud.setId(SOLICITUD_ID);
                    return solicitud;
                });

        when(notificacionRepository.save(any(Notificacion.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        SolicitudEmergenciaResponse respuesta = emergenciaService.solicitar(
                new SolicitudEmergenciaRequest(
                        TipoEmergencia.OTRA, 
                        "  ",
                        null,
                        null
                )
        );

        assertNull(respuesta.ubicacion());
    }

    @Test
    void historial_DevuelveSolicitudesDelUsuarioAutenticado() {
        SolicitudEmergencia solicitud = solicitud(EstadoEmergencia.ENVIADA);

        when(solicitudRepository.findByUsuarioIdOrderByFechaHoraDesc(USUARIO_ID))
                .thenReturn(List.of(solicitud));

        List<SolicitudEmergenciaResponse> historial = emergenciaService.historial();

        assertEquals(1, historial.size());
        assertEquals(SOLICITUD_ID, historial.get(0).id());
        assertEquals(EstadoEmergencia.ENVIADA, historial.get(0).estado());

        verify(solicitudRepository)
                .findByUsuarioIdOrderByFechaHoraDesc(USUARIO_ID);
    }

    @Test
    void obtener_DevuelveLaSolicitudDelUsuario() {
        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud(EstadoEmergencia.ENVIADA)));

        SolicitudEmergenciaResponse respuesta = emergenciaService.obtener(SOLICITUD_ID);

        assertEquals(SOLICITUD_ID, respuesta.id());
        assertEquals(EstadoEmergencia.ENVIADA, respuesta.estado());
    }

    @Test
    void obtener_NoPermiteConsultarSolicitudDeOtroUsuario() {
        Usuario otroUsuario = Usuario.builder()
                .id(OTRO_USUARIO_ID)
                .nombre("Otro usuario")
                .build();

        SolicitudEmergencia solicitud = SolicitudEmergencia.builder()
                .id(SOLICITUD_ID)
                .usuario(otroUsuario)
                .contactoEmergencia(contacto)
                .estado(EstadoEmergencia.ENVIADA)
                .tipo(TipoEmergencia.MEDICA)
                .build();

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        assertThrows(ResourceNotFoundException.class, () ->
                emergenciaService.obtener(SOLICITUD_ID)
        );
    }

    @Test
    void cancelar_CambiaEstadoDePendienteACancelada() {
        SolicitudEmergencia solicitud = solicitud(EstadoEmergencia.PENDIENTE);

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        SolicitudEmergenciaResponse respuesta = emergenciaService.cancelar(SOLICITUD_ID);

        assertEquals(EstadoEmergencia.CANCELADA, respuesta.estado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void cancelar_NoPermiteCancelarSolicitudEnviada() {
        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud(EstadoEmergencia.ENVIADA)));

        assertThrows(BusinessException.class, () ->
                emergenciaService.cancelar(SOLICITUD_ID)
        );

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void marcarAtendida_CambiaEstadoDeEnviadaAAtendida() {
        SolicitudEmergencia solicitud = solicitud(EstadoEmergencia.ENVIADA);

        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud));

        when(solicitudRepository.save(solicitud)).thenReturn(solicitud);

        SolicitudEmergenciaResponse respuesta =
                emergenciaService.marcarAtendida(SOLICITUD_ID);

        assertEquals(EstadoEmergencia.ATENDIDA, respuesta.estado());
        verify(solicitudRepository).save(solicitud);
    }

    @Test
    void marcarAtendida_NoPermiteAtenderSolicitudPendiente() {
        when(solicitudRepository.findById(SOLICITUD_ID))
                .thenReturn(Optional.of(solicitud(EstadoEmergencia.PENDIENTE)));

        assertThrows(BusinessException.class, () ->
                emergenciaService.marcarAtendida(SOLICITUD_ID)
        );

        verify(solicitudRepository, never()).save(any());
    }

    @Test
    void telefonoInternacional_agregaIndicativoACelularesDeDiezDigitos() {
        // Contactos registrados al crear la cuenta (sin +57) y desde la pantalla de contacto (con +57)
        assertEquals("+573001234567", EmergenciaServiceImpl.telefonoInternacional("3001234567"));
        assertEquals("+573001234567", EmergenciaServiceImpl.telefonoInternacional("+573001234567"));
        assertEquals("+573001234567", EmergenciaServiceImpl.telefonoInternacional("300 123 4567"));
        assertEquals("+573001234567", EmergenciaServiceImpl.telefonoInternacional("573001234567"));
    }
}