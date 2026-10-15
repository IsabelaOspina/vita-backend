package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.vitabackend.domain.enums.EstadoActividad;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.TipoActividad;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecordatorioServiceImplTest {
    private static final String CORREO = "usuario@vita.test";
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private ActividadSaludRepository actividadRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RecordatorioMapper mapper;
    @InjectMocks private RecordatorioServiceImpl service;

    private Usuario usuario;
    private ActividadSalud actividad;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(8L).correo(CORREO).build();
        actividad = ActividadSalud.builder().id(3L).usuario(usuario)
                .nombre("Cita médica").tipo(TipoActividad.CITA_MEDICA)
                .fechaHora(LocalDateTime.now().plusDays(2))
                .estado(EstadoActividad.PENDIENTE).build();
        when(usuarioRepository.findByCorreo(CORREO)).thenReturn(Optional.of(usuario));
    }

    @Test
    void crearParaActividad_AsociaActividadYDejaTomaNula() {
        LocalDateTime fecha = LocalDateTime.now().plusHours(1);
        RecordatorioRequest request = new RecordatorioRequest(fecha, "Recordar cita", TipoRecordatorio.VOCAL);
        when(actividadRepository.findByIdAndUsuario_Id(3L, 8L)).thenReturn(Optional.of(actividad));
        when(recordatorioRepository.save(any(Recordatorio.class))).thenAnswer(i -> {
            Recordatorio r = i.getArgument(0); r.setId(90L); return r;
        });
        when(mapper.toResponse(any(Recordatorio.class))).thenAnswer(i -> {
            Recordatorio r = i.getArgument(0);
            return new RecordatorioResponse(r.getId(), r.getFechaHora(), r.getMensaje(), r.getTipoRecordatorio(),
                    r.getEstado(), r.getActividadSalud().getId(), null);
        });

        RecordatorioResponse response = service.crearParaActividad(CORREO, 3L, request);

        assertEquals(90L, response.id());
        assertEquals(3L, response.actividadSaludId());
        assertNull(response.tomaMedicamentoId());
        verify(recordatorioRepository).save(argThat(r -> r.getActividadSalud().getId().equals(3L)
                && r.getTomaMedicamento() == null && r.getEstado() == EstadoRecordatorio.PENDIENTE));
    }

    @Test
    void crearParaActividad_RechazaRecordatorioPosteriorALaActividad() {
        actividad.setFechaHora(LocalDateTime.now().plusMinutes(5));
        when(actividadRepository.findByIdAndUsuario_Id(3L, 8L)).thenReturn(Optional.of(actividad));
        RecordatorioRequest request = new RecordatorioRequest(
                LocalDateTime.now().plusMinutes(10), "Recordar", TipoRecordatorio.SONORA);
        assertThrows(BusinessException.class, () -> service.crearParaActividad(CORREO, 3L, request));
        verify(recordatorioRepository, never()).save(any());
    }

    @Test
    void crearParaActividad_NoRevelaActividadDeOtroUsuario() {
        when(actividadRepository.findByIdAndUsuario_Id(3L, 8L)).thenReturn(Optional.empty());
        RecordatorioRequest request = new RecordatorioRequest(
                LocalDateTime.now().plusHours(1), "Recordar", TipoRecordatorio.VISUAL);
        assertThrows(ResourceNotFoundException.class, () -> service.crearParaActividad(CORREO, 3L, request));
    }

    @Test
    void cancelar_CambiaEstadoPendienteACancelado() {
        Recordatorio recordatorio = Recordatorio.builder().id(90L).estado(EstadoRecordatorio.PENDIENTE)
                .actividadSalud(actividad).fechaHora(LocalDateTime.now().plusHours(1))
                .tipoRecordatorio(TipoRecordatorio.VISUAL).mensaje("Recordar").build();
        when(recordatorioRepository.findPropioById(90L, 8L)).thenReturn(Optional.of(recordatorio));
        when(recordatorioRepository.save(recordatorio)).thenReturn(recordatorio);
        when(mapper.toResponse(recordatorio)).thenReturn(new RecordatorioResponse(90L, recordatorio.getFechaHora(),
                recordatorio.getMensaje(), recordatorio.getTipoRecordatorio(), EstadoRecordatorio.CANCELADO, 3L, null));

        RecordatorioResponse result = service.cancelar(CORREO, 90L);
        assertEquals(EstadoRecordatorio.CANCELADO, result.estado());
        assertEquals(EstadoRecordatorio.CANCELADO, recordatorio.getEstado());
    }
}
