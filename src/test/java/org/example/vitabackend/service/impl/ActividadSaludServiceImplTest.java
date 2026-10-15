package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.TipoActividad;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActividadSaludServiceImplTest {

    private static final String CORREO = "usuario@vita.test";
    private static final Long USUARIO_ID = 7L;
    private static final Long ACTIVIDAD_ID = 31L;

    @Mock
    private ActividadSaludRepository actividadSaludRepository;
    @Mock
    private RecordatorioRepository recordatorioRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ActividadSaludMapper actividadSaludMapper;

    @InjectMocks
    private ActividadSaludServiceImpl service;

    private Usuario usuario;
    private ActividadSalud actividad;
    private ActividadSaludRequest request;
    private ActividadSaludResponse response;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(USUARIO_ID)
                .correo(CORREO)
                .nombre("Usuario de prueba")
                .activo(true)
                .build();

        request = new ActividadSaludRequest(
                "Cita de medicina general",
                TipoActividad.CITA_MEDICA,
                LocalDateTime.now().plusDays(2).withNano(0),
                "Hospital de prueba",
                "Control anual");

        actividad = ActividadSalud.builder()
                .id(ACTIVIDAD_ID)
                .nombre(request.nombre())
                .tipo(request.tipo())
                .fechaHora(request.fechaHora())
                .ubicacion(request.ubicacion())
                .descripcion(request.descripcion())
                .estado(EstadoActividad.PENDIENTE)
                .usuario(usuario)
                .build();

        response = new ActividadSaludResponse(
                ACTIVIDAD_ID,
                request.nombre(),
                request.tipo(),
                request.fechaHora(),
                request.ubicacion(),
                request.descripcion(),
                EstadoActividad.PENDIENTE);
    }

    @Test
    void crear_AsociaElUsuarioAutenticadoYEstablecePendiente() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludMapper.toEntity(request)).thenReturn(actividad);
        when(actividadSaludRepository.save(any(ActividadSalud.class))).thenReturn(actividad);
        when(actividadSaludMapper.toResponse(actividad)).thenReturn(response);

        ActividadSaludResponse resultado = service.crear(CORREO, request);

        assertEquals(ACTIVIDAD_ID, resultado.id());
        assertEquals(EstadoActividad.PENDIENTE, resultado.estado());
        ArgumentCaptor<ActividadSalud> captor = ArgumentCaptor.forClass(ActividadSalud.class);
        verify(actividadSaludRepository).save(captor.capture());
        assertEquals(USUARIO_ID, captor.getValue().getUsuario().getId());
        assertEquals(EstadoActividad.PENDIENTE, captor.getValue().getEstado());
    }

    @Test
    void listar_ConsultaSoloLasActividadesDelUsuarioYMapeaLaRespuesta() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByUsuario_IdOrderByFechaHoraAscIdAsc(USUARIO_ID))
                .thenReturn(List.of(actividad));
        when(actividadSaludMapper.toResponse(actividad)).thenReturn(response);

        List<ActividadSaludResponse> resultado = service.listar(CORREO);

        assertEquals(1, resultado.size());
        assertEquals(ACTIVIDAD_ID, resultado.getFirst().id());
        verify(actividadSaludRepository).findByUsuario_IdOrderByFechaHoraAscIdAsc(USUARIO_ID);
    }

    @Test
    void listarPorRango_RechazaRangoInvertidoSinConsultarDatos() {
        LocalDateTime desde = LocalDateTime.of(2026, 10, 20, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2026, 10, 19, 23, 59);

        assertThrows(BusinessException.class,
                () -> service.listarPorRango(CORREO, desde, hasta));
        verify(usuarioRepository, never()).findByCorreo(any());
        verify(actividadSaludRepository, never()).findByUsuario_IdAndFechaHoraBetweenOrderByFechaHoraAscIdAsc(
                anyLong(), any(), any());
    }

    @Test
    void obtener_NoPermiteAccederAActividadDeOtroUsuario() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtener(CORREO, ACTIVIDAD_ID));
    }

    @Test
    void actualizar_ConservaEstadoYUsaMapperParaEditarCamposPermitidos() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));
        doNothing().when(actividadSaludMapper).actualizarDesdeRequest(request, actividad);
        when(actividadSaludRepository.save(actividad)).thenReturn(actividad);
        when(actividadSaludMapper.toResponse(actividad)).thenReturn(response);

        service.actualizar(CORREO, ACTIVIDAD_ID, request);

        verify(actividadSaludMapper).actualizarDesdeRequest(request, actividad);
        assertEquals(EstadoActividad.PENDIENTE, actividad.getEstado());
        assertEquals(USUARIO_ID, actividad.getUsuario().getId());
    }

    @Test
    void completar_CambiaEstadoACompletada() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));
        when(actividadSaludRepository.save(actividad)).thenReturn(actividad);
        when(actividadSaludMapper.toResponse(actividad)).thenAnswer(invocation ->
                new ActividadSaludResponse(actividad.getId(), actividad.getNombre(), actividad.getTipo(),
                        actividad.getFechaHora(), actividad.getUbicacion(), actividad.getDescripcion(),
                        actividad.getEstado()));

        ActividadSaludResponse resultado = service.completar(CORREO, ACTIVIDAD_ID);

        assertEquals(EstadoActividad.COMPLETADA, actividad.getEstado());
        assertEquals(EstadoActividad.COMPLETADA, resultado.estado());
        verify(actividadSaludRepository).save(actividad);
    }

    @Test
    void completar_RechazaActividadCancelada() {
        actividad.setEstado(EstadoActividad.CANCELADA);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));

        assertThrows(BusinessException.class, () -> service.completar(CORREO, ACTIVIDAD_ID));
        verify(actividadSaludRepository, never()).save(actividad);
    }

    @Test
    void cancelar_CancelaActividadYRecordatoriosPendientesFuturos() {
        Recordatorio recordatorio = Recordatorio.builder()
                .id(91L)
                .fechaHora(LocalDateTime.now().plusDays(1))
                .estado(EstadoRecordatorio.PENDIENTE)
                .actividadSalud(actividad)
                .build();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));
        when(actividadSaludRepository.save(actividad)).thenReturn(actividad);
        when(recordatorioRepository.findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
                eq(ACTIVIDAD_ID), eq(EstadoRecordatorio.PENDIENTE), any(LocalDateTime.class)))
                .thenReturn(List.of(recordatorio));
        when(recordatorioRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.cancelar(CORREO, ACTIVIDAD_ID);

        assertEquals(EstadoActividad.CANCELADA, actividad.getEstado());
        assertEquals(EstadoRecordatorio.CANCELADO, recordatorio.getEstado());
        verify(actividadSaludRepository).save(actividad);
        verify(recordatorioRepository).saveAll(List.of(recordatorio));
    }

    @Test
    void cancelar_ActividadCompletadaNoPuedeCancelarse() {
        actividad.setEstado(EstadoActividad.COMPLETADA);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));

        assertThrows(BusinessException.class, () -> service.cancelar(CORREO, ACTIVIDAD_ID));
        verify(recordatorioRepository, never()).saveAll(any());
    }

    @Test
    void cancelar_ActividadYaCanceladaEsIdempotente() {
        actividad.setEstado(EstadoActividad.CANCELADA);
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));

        service.cancelar(CORREO, ACTIVIDAD_ID);

        verify(actividadSaludRepository, never()).save(actividad);
        verify(recordatorioRepository, never()).saveAll(any());
        assertTrue(actividad.getEstado() == EstadoActividad.CANCELADA);
    }
    @Test
    void crear_RechazaFechaAnteriorAlMomentoActual() {
        request = new ActividadSaludRequest(
                "Control atrasado", TipoActividad.CITA_MEDICA,
                LocalDateTime.now().minusMinutes(1), "Hospital", "Prueba");
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));

        assertThrows(BusinessException.class, () -> service.crear(CORREO, request));
        verify(actividadSaludRepository, never()).save(any(ActividadSalud.class));
    }

    @Test
    void actualizar_RechazaFechaAnteriorAlMomentoActual() {
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
        when(actividadSaludRepository.findByIdAndUsuario_Id(ACTIVIDAD_ID, USUARIO_ID))
                .thenReturn(Optional.of(actividad));
        request = new ActividadSaludRequest(
                "Cita", TipoActividad.CITA_MEDICA,
                LocalDateTime.now().minusMinutes(1), "Hospital", "Prueba");

        assertThrows(BusinessException.class, () -> service.actualizar(CORREO, ACTIVIDAD_ID, request));
        verify(actividadSaludMapper, never()).actualizarDesdeRequest(any(), any());
    }

}
