package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.repository.HorarioMedicamentoRepository;
import org.example.vitabackend.repository.MedicamentoRepository;
import org.example.vitabackend.repository.RecordatorioRepository;
import org.example.vitabackend.repository.TomaMedicamentoRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TomaMedicamentoEstadoTest {
    @Mock private HorarioMedicamentoRepository horarioRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private TomaMedicamentoRepository tomaRepository;
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TomaMedicamentoOccurrenceCalculator calculator;
    @InjectMocks private HorarioMedicamentoServiceImpl service;

    @Test
    void notificadoNoSignificaTomado() {
        Usuario usuario = Usuario.builder().id(1L).correo("u@vita.test").build();
        Medicamento medicamento = Medicamento.builder().id(2L).nombre("Medicamento").usuario(usuario).activo(true).build();
        TomaMedicamento toma = TomaMedicamento.builder().id(3L).medicamento(medicamento)
                .fechaHora(LocalDateTime.now().plusMinutes(1)).estado(EstadoTomaMed.PENDIENTE).build();
        Recordatorio aviso = Recordatorio.builder().id(4L).tomaMedicamento(toma)
                .estado(EstadoRecordatorio.PENDIENTE).tipoRecordatorio(TipoRecordatorio.VISUAL).build();
        when(usuarioRepository.findByCorreo("u@vita.test")).thenReturn(Optional.of(usuario));
        when(tomaRepository.findByIdAndMedicamento_Usuario_Id(3L, 1L)).thenReturn(Optional.of(toma));
        when(recordatorioRepository.findByTomaMedicamento_Id(3L)).thenReturn(List.of(aviso));
        when(tomaRepository.save(toma)).thenReturn(toma);
        when(recordatorioRepository.save(aviso)).thenReturn(aviso);

        service.marcarNotificado("u@vita.test", 3L);

        assertEquals(EstadoTomaMed.NOTIFICADO, toma.getEstado());
        assertEquals(EstadoRecordatorio.ENVIADO, aviso.getEstado());
        verify(tomaRepository).save(toma);
    }

    @Test
    void soloAccionTomadoMarcaTomaComoTomada() {
        Usuario usuario = Usuario.builder().id(1L).correo("u@vita.test").build();
        Medicamento medicamento = Medicamento.builder().id(2L).nombre("Medicamento").usuario(usuario).activo(true).build();
        TomaMedicamento toma = TomaMedicamento.builder().id(3L).medicamento(medicamento)
                .fechaHora(LocalDateTime.now().plusMinutes(1)).estado(EstadoTomaMed.NOTIFICADO).build();
        Recordatorio aviso = Recordatorio.builder().id(4L).tomaMedicamento(toma)
                .estado(EstadoRecordatorio.ENVIADO).tipoRecordatorio(TipoRecordatorio.VISUAL).build();
        when(usuarioRepository.findByCorreo("u@vita.test")).thenReturn(Optional.of(usuario));
        when(tomaRepository.findByIdAndMedicamento_Usuario_Id(3L, 1L)).thenReturn(Optional.of(toma));
        when(recordatorioRepository.findByTomaMedicamento_Id(3L)).thenReturn(List.of(aviso));
        when(tomaRepository.save(toma)).thenReturn(toma);
        when(recordatorioRepository.save(aviso)).thenReturn(aviso);

        service.marcarTomado("u@vita.test", 3L);

        assertEquals(EstadoTomaMed.TOMADO, toma.getEstado());
        assertEquals(EstadoRecordatorio.CONFIRMADO, aviso.getEstado());
    }
}
