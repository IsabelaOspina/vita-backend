package org.example.vitabackend.config;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Amplía diariamente el horizonte de ocurrencias para reglas recurrentes activas. */
@Component
@RequiredArgsConstructor
public class TomaMedicamentoScheduler {
    private final HorarioMedicamentoService horarioService;

    @Scheduled(cron = "0 5 0 * * *", zone = "America/Bogota")
    public void extenderHorizonte() {
        horarioService.extenderHorizonteActivo();
    }
}
