package org.example.vitabackend.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.response.AsignacionResponse;
import org.example.vitabackend.service.AsignacionService;
import org.example.vitabackend.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints del profesional de la salud (SecurityConfig exige rol PERSONAL_SALUD).
 */
@RestController
@RequestMapping("/api/personal-salud")
@RequiredArgsConstructor
public class PersonalSaludController {

    private final AsignacionService asignacionService;
    private final UsuarioService usuarioService;

    /** Pacientes que tiene asignados el profesional que inició sesión. */
    @GetMapping("/me/pacientes")
    public List<AsignacionResponse> misPacientes(Authentication authentication) {
        Long miId = usuarioService.obtenerPorCorreo(authentication.getName()).id();
        return asignacionService.pacientesDe(miId);
    }
}
