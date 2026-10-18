package org.example.vitabackend.controller;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.response.TomaMedicamentoResponse;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Cambios de estado de ocurrencias concretas, siempre comprobando su propietario. */
@RestController
@RequestMapping("/api/tomas-medicamento")
@RequiredArgsConstructor
public class TomaMedicamentoController {
    private final HorarioMedicamentoService service;

    /**
     * Todas las tomas del usuario autenticado en un rango (por ejemplo, las de hoy), de sus medicamentos
     * y horarios activos. Reemplaza consultar medicamento por medicamento y horario por horario.
     */
    @GetMapping
    public List<TomaMedicamentoResponse> listar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication auth) {
        try {
            service.asegurarHorizonte(auth.getName());
        } catch (RuntimeException e) {
            // Si dos consultas llegan al tiempo, una puede chocar con las tomas que crea la otra:
            // no impide responder y se completa en la próxima consulta
        }
        return service.listarTomasDelUsuario(auth.getName(), desde, hasta);
    }

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
