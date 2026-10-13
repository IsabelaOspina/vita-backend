package org.example.vitabackend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.response.AsignacionResponse;
import org.example.vitabackend.service.AsignacionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Asignación de pacientes a profesionales. Solo ADMIN (SecurityConfig protege /api/admin/**).
 * Los ids son los de las cuentas (usuario.id).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping("/personal-salud/{profesionalId}/pacientes/{pacienteId}")
    @ResponseStatus(HttpStatus.CREATED)
    public AsignacionResponse asignar(@PathVariable Long profesionalId, @PathVariable Long pacienteId) {
        return asignacionService.asignar(profesionalId, pacienteId);
    }

    @DeleteMapping("/personal-salud/{profesionalId}/pacientes/{pacienteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desasignar(@PathVariable Long profesionalId, @PathVariable Long pacienteId) {
        asignacionService.desasignar(profesionalId, pacienteId);
    }

    @GetMapping("/personal-salud/{profesionalId}/pacientes")
    public List<AsignacionResponse> pacientesDe(@PathVariable Long profesionalId) {
        return asignacionService.pacientesDe(profesionalId);
    }

    @GetMapping("/pacientes/{pacienteId}/personal-salud")
    public List<AsignacionResponse> profesionalesDe(@PathVariable Long pacienteId) {
        return asignacionService.profesionalesDe(pacienteId);
    }
}
