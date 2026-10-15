package org.example.vitabackend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.example.vitabackend.domain.model.Medicamento;
import org.junit.jupiter.api.Test;

class TomaMedicamentoOccurrenceCalculatorTest {
    private final TomaMedicamentoOccurrenceCalculator calculator = new TomaMedicamentoOccurrenceCalculator();
    private final Medicamento medicamento = Medicamento.builder().id(1L).nombre("Acetaminofén").activo(true).build();

    @Test
    void cadaOchoHoras_GeneraOcurrenciasDentroDelRango() {
        HorarioMedicamento h = HorarioMedicamento.builder().id(2L).medicamento(medicamento)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.CADA_X_HORAS)
                .fechaHoraInicio(LocalDateTime.of(2026, 10, 14, 8, 0)).intervaloHoras(8)
                .tipoRecordatorio(TipoRecordatorio.VOCAL).activo(true).build();
        assertEquals(java.util.List.of(LocalDateTime.of(2026, 10, 14, 16, 0),
                LocalDateTime.of(2026, 10, 15, 0, 0), LocalDateTime.of(2026, 10, 15, 8, 0)),
                calculator.calcular(h, LocalDateTime.of(2026, 10, 14, 12, 0),
                        LocalDateTime.of(2026, 10, 15, 12, 0)));
    }

    @Test
    void diario_GeneraUnaTomaCadaDiaALaHoraElegida() {
        HorarioMedicamento h = HorarioMedicamento.builder().medicamento(medicamento)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.DIARIO)
                .fechaHoraInicio(LocalDateTime.of(2026, 10, 14, 0, 0)).horaProgramada(LocalTime.of(17, 0))
                .tipoRecordatorio(TipoRecordatorio.VISUAL).activo(true).build();
        assertEquals(3, calculator.calcular(h, LocalDateTime.of(2026, 10, 14, 0, 0),
                LocalDateTime.of(2026, 10, 17, 0, 0)).size());
    }

    @Test
    void diasSemana_SoloGeneraLosDiasSeleccionados() {
        HorarioMedicamento h = HorarioMedicamento.builder().medicamento(medicamento)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.DIAS_SEMANA)
                .fechaHoraInicio(LocalDateTime.of(2026, 10, 12, 0, 0)).horaProgramada(LocalTime.of(17, 0))
                .diasSemana(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
                .tipoRecordatorio(TipoRecordatorio.SONORA).activo(true).build();
        assertEquals(java.util.List.of(LocalDateTime.of(2026, 10, 12, 17, 0),
                LocalDateTime.of(2026, 10, 14, 17, 0), LocalDateTime.of(2026, 10, 19, 17, 0)),
                calculator.calcular(h, LocalDateTime.of(2026, 10, 12, 0, 0),
                        LocalDateTime.of(2026, 10, 20, 0, 0)));
    }

    @Test
    void noIncluyeElLimiteSuperior() {
        HorarioMedicamento h = HorarioMedicamento.builder().medicamento(medicamento)
                .tipoFrecuencia(TipoFrecuenciaMedicamento.DIARIO)
                .fechaHoraInicio(LocalDateTime.of(2026, 10, 14, 0, 0)).horaProgramada(LocalTime.NOON)
                .tipoRecordatorio(TipoRecordatorio.VISUAL).activo(true).build();
        assertEquals(0, calculator.calcular(h, LocalDateTime.of(2026, 10, 14, 0, 0),
                LocalDateTime.of(2026, 10, 14, 12, 0)).size());
    }
}
