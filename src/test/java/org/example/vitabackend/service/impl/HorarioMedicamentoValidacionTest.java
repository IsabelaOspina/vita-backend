package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.Medicamento;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.HorarioMedicamentoRequest;
import org.example.vitabackend.exception.BusinessException;
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
class HorarioMedicamentoValidacionTest {
    @Mock private HorarioMedicamentoRepository horarioRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private TomaMedicamentoRepository tomaRepository;
    @Mock private RecordatorioRepository recordatorioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TomaMedicamentoOccurrenceCalculator calculator;
    @InjectMocks private HorarioMedicamentoServiceImpl service;

    @Test
    void intervaloHoras_ExigeIntervalo() {
        Usuario u = Usuario.builder().id(1L).correo("u@vita.test").build();
        Medicamento m = Medicamento.builder().id(2L).usuario(u).activo(true).build();
        when(usuarioRepository.findByCorreo("u@vita.test")).thenReturn(Optional.of(u));
        when(medicamentoRepository.findByIdAndUsuario_Id(2L, 1L)).thenReturn(Optional.of(m));
        HorarioMedicamentoRequest request = new HorarioMedicamentoRequest(TipoFrecuenciaMedicamento.CADA_X_HORAS,
                LocalDateTime.now().plusMinutes(1), null, null, null, Set.of(), TipoRecordatorio.VISUAL);
        assertThrows(BusinessException.class, () -> service.crear("u@vita.test", 2L, request));
    }

    @Test
    void diasSemana_ExigeAlMenosUnDia() {
        Usuario u = Usuario.builder().id(1L).correo("u@vita.test").build();
        Medicamento m = Medicamento.builder().id(2L).usuario(u).activo(true).build();
        when(usuarioRepository.findByCorreo("u@vita.test")).thenReturn(Optional.of(u));
        when(medicamentoRepository.findByIdAndUsuario_Id(2L, 1L)).thenReturn(Optional.of(m));
        HorarioMedicamentoRequest request = new HorarioMedicamentoRequest(TipoFrecuenciaMedicamento.DIAS_SEMANA,
                LocalDateTime.now().plusMinutes(1), null, null, LocalTime.of(17, 0), Set.of(), TipoRecordatorio.SONORA);
        assertThrows(BusinessException.class, () -> service.crear("u@vita.test", 2L, request));
    }
}
