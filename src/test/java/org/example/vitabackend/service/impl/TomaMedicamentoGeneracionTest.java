package org.example.vitabackend.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.EstadoRecordatorio;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Recordatorio;
import org.example.vitabackend.domain.model.TomaMedicamento;
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
class TomaMedicamentoGeneracionTest {
    @Mock private HorarioMedicamentoRepository horarioRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private TomaMedicamentoRepository tomaRepository;
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TomaMedicamentoOccurrenceCalculator calculator;
    @InjectMocks private HorarioMedicamentoServiceImpl service;

    @Test
    void cadaOcurrenciaGeneraTomaYUnRecordatorioVinculadoSoloALaToma() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 20, 16, 0);
        Medicamento m = Medicamento.builder().id(4L).nombre("Acetaminofén").dosis("500 mg").activo(true).build();
        HorarioMedicamento h = HorarioMedicamento.builder().id(9L).medicamento(m)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.CADA_X_HORAS).fechaHoraInicio(fecha)
                .intervaloHoras(8).tipoRecordatorio(TipoRecordatorio.VOCAL).activo(true).build();
        when(calculator.calcular(h, fecha, fecha.plusHours(1))).thenReturn(java.util.List.of(fecha));
        when(tomaRepository.findFechasDeHorarioEntre(9L, fecha, fecha.plusHours(1))).thenReturn(java.util.List.of());
        when(tomaRepository.save(any(TomaMedicamento.class))).thenAnswer(invocation -> {
            TomaMedicamento t = invocation.getArgument(0); t.setId(12L); return t;
        });
        when(recordatorioRepository.save(any(Recordatorio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.generarOcurrencias(h, fecha, fecha.plusHours(1));

        verify(tomaRepository).save(argThat(t -> t.getEstado() == EstadoTomaMed.PENDIENTE
                && t.getMedicamento().getId().equals(4L) && t.getHorarioMedicamento().getId().equals(9L)));
        verify(recordatorioRepository).save(argThat(r -> r.getTomaMedicamento().getId().equals(12L)
                && r.getActividadSalud() == null
                && r.getEstado() == EstadoRecordatorio.PENDIENTE
                && r.getTipoRecordatorio() == TipoRecordatorio.VOCAL
                && r.getMensaje().contains("Acetaminofén")));
    }

    @Test
    void noDuplicaTomasQueYaExisten() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 20, 8, 0);
        Medicamento m = Medicamento.builder().id(4L).nombre("Losartán").activo(true).build();
        HorarioMedicamento h = HorarioMedicamento.builder().id(9L).medicamento(m)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.DIARIO).fechaHoraInicio(fecha)
                .tipoRecordatorio(TipoRecordatorio.VISUAL).activo(true).build();
        when(calculator.calcular(h, fecha, fecha.plusDays(2))).thenReturn(java.util.List.of(fecha, fecha.plusDays(1)));
        when(tomaRepository.findFechasDeHorarioEntre(9L, fecha, fecha.plusDays(2))).thenReturn(java.util.List.of(fecha));
        when(tomaRepository.save(any(TomaMedicamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(recordatorioRepository.save(any(Recordatorio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.generarOcurrencias(h, fecha, fecha.plusDays(2));

        // Solo se crea la del día siguiente: la primera ya existía
        verify(tomaRepository, times(1)).save(argThat(t -> t.getFechaHora().equals(fecha.plusDays(1))));
        verify(recordatorioRepository, times(1)).save(any(Recordatorio.class));
    }
}