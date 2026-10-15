package org.example.vitabackend.controller;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.response.TomaMedicamentoResponse;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Cambios de estado de ocurrencias concretas, siempre comprobando su propietario. */
@RestController
@RequestMapping("/api/tomas-medicamento")
@RequiredArgsConstructor
public class TomaMedicamentoController {
    private final HorarioMedicamentoService service;

    @PatchMapping("/{id}/notificado")
    public TomaMedicamentoResponse notificado(@PathVariable Long id, Authentication auth) {
        return service.marcarNotificado(auth.getName(), id);
    }

    /** TOMADO solo se registra después de una acción/confirmación explícita del usuario. */
    @PatchMapping("/{id}/tomado")
    public TomaMedicamentoResponse tomado(@PathVariable Long id, Authentication auth) {
        return service.marcarTomado(auth.getName(), id);
    }
}
