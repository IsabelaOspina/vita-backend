package org.example.vitabackend.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.springframework.stereotype.Component;

/** Componente puro para calcular ocurrencias sin acceder a la base de datos. */
@Component
public class TomaMedicamentoOccurrenceCalculator {

    /** Devuelve ocurrencias de [desde, hastaExclusivo), respetando el rango del horario. */
    public List<LocalDateTime> calcular(HorarioMedicamento horario,
                                        LocalDateTime desde, LocalDateTime hastaExclusivo) {
        if (horario == null || desde == null || hastaExclusivo == null || !desde.isBefore(hastaExclusivo)) {
            return List.of();
        }
        return switch (horario.getTipoFrecuencia()) {
            case CADA_X_HORAS -> calcularIntervalo(horario, desde, hastaExclusivo);
            case DIARIO -> calcularDias(horario, desde, hastaExclusivo, Set.of());
            case DIAS_SEMANA -> calcularDias(horario, desde, hastaExclusivo,
                    horario.getDiasSemana() == null ? Set.of() : horario.getDiasSemana());
        };
    }

    private List<LocalDateTime> calcularIntervalo(
            HorarioMedicamento horario, LocalDateTime desde, LocalDateTime hasta) {
        List<LocalDateTime> resultado = new ArrayList<>();
        int horas = horario.getIntervaloHoras();
        LocalDateTime inicio = horario.getFechaHoraInicio();
        long segundosPaso = Duration.ofHours(horas).getSeconds();
        long segundosHastaDesde = Duration.between(inicio, desde).getSeconds();
        long saltos = segundosHastaDesde <= 0 ? 0 : (segundosHastaDesde + segundosPaso - 1) / segundosPaso;
        LocalDateTime candidata = inicio.plusHours(saltos * horas);
        while (candidata.isBefore(hasta) && dentroDelFin(horario, candidata)) {
            if (!candidata.isBefore(desde)) resultado.add(candidata);
            candidata = candidata.plusHours(horas);
        }
        return resultado;
    }

    private List<LocalDateTime> calcularDias(
            HorarioMedicamento horario, LocalDateTime desde, LocalDateTime hasta, Set<DayOfWeek> dias) {
        List<LocalDateTime> resultado = new ArrayList<>();
        LocalDate inicioRegla = horario.getFechaHoraInicio().toLocalDate();
        LocalDate fecha = desde.toLocalDate().isAfter(inicioRegla) ? desde.toLocalDate() : inicioRegla;
        LocalTime horaProgramada = horario.getHoraProgramada();
        while (true) {
            LocalDateTime candidata = fecha.atTime(horaProgramada);
            if (!candidata.isBefore(hasta)) break;
            if (!fecha.isBefore(inicioRegla)
                    && !candidata.isBefore(horario.getFechaHoraInicio())
                    && !candidata.isBefore(desde)
                    && dentroDelFin(horario, candidata)
                    && (horario.getTipoFrecuencia() == TipoFrecuenciaMedicamento.DIARIO
                        || dias.contains(fecha.getDayOfWeek()))) {
                resultado.add(candidata);
            }
            if (horario.getFechaHoraFin() != null
                    && fecha.isAfter(horario.getFechaHoraFin().toLocalDate())) break;
            fecha = fecha.plusDays(1);
        }
        return resultado;
    }

    private boolean dentroDelFin(HorarioMedicamento horario, LocalDateTime candidata) {
        return horario.getFechaHoraFin() == null || !candidata.isAfter(horario.getFechaHoraFin());
    }

}
