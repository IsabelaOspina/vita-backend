package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import org.example.vitabackend.mapper.ActividadSaludMapper;
import org.example.vitabackend.repository.ActividadSaludRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActividadCambioFechaRecordatoriosTest {
    @Mock private ActividadSaludRepository actividadRepository;
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ActividadSaludMapper mapper;
    @InjectMocks private ActividadSaludServiceImpl service;

    @Test
    void cambiarFechaReprogramaLosRecordatoriosPendientesFuturos() {
        String correo = "usuario@vita.test";
        Usuario usuario = Usuario.builder().id(10L).correo(correo).build();
        LocalDateTime anterior = LocalDateTime.now().plusDays(2).withNano(0);
        LocalDateTime nueva = LocalDateTime.now().plusDays(5).withNano(0);
        ActividadSalud actividad = ActividadSalud.builder().id(7L).usuario(usuario).nombre("Cita")
                .tipo(TipoActividad.CITA_MEDICA).fechaHora(anterior).estado(EstadoActividad.PENDIENTE).build();
        ActividadSaludRequest request = new ActividadSaludRequest("Cita", TipoActividad.CITA_MEDICA,
                nueva, "Hospital", "Control");
        Recordatorio recordatorio = Recordatorio.builder().id(88L).actividadSalud(actividad)
                .fechaHora(anterior.minusHours(2)).estado(EstadoRecordatorio.PENDIENTE).build();

        when(usuarioRepository.findByCorreo(correo)).thenReturn(Optional.of(usuario));
        when(actividadRepository.findByIdAndUsuario_Id(7L, 10L)).thenReturn(Optional.of(actividad));
        doAnswer(invocation -> {
            ActividadSaludRequest r = invocation.getArgument(0);
            ActividadSalud target = invocation.getArgument(1);
            target.setFechaHora(r.fechaHora());
            return null;
        }).when(mapper).actualizarDesdeRequest(request, actividad);
        when(recordatorioRepository.findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
                eq(7L), eq(EstadoRecordatorio.PENDIENTE), any(LocalDateTime.class))).thenReturn(List.of(recordatorio));
        when(recordatorioRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        when(actividadRepository.save(actividad)).thenReturn(actividad);
        when(mapper.toResponse(actividad)).thenReturn(new ActividadSaludResponse(7L, "Cita", TipoActividad.CITA_MEDICA,
                nueva, "Hospital", "Control", EstadoActividad.PENDIENTE));

        service.actualizar(correo, 7L, request);

        assertEquals(EstadoRecordatorio.PENDIENTE, recordatorio.getEstado());
        assertEquals(nueva.minusHours(2), recordatorio.getFechaHora());
        verify(recordatorioRepository).saveAll(List.of(recordatorio));
    }
}