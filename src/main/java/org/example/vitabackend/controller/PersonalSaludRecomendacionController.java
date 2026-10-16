package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.RecomendacionRequest;
import org.example.vitabackend.dto.response.PaginaResponse;
import org.example.vitabackend.dto.response.RecomendacionResponse;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.RecomendacionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * El profesional de la salud deja recomendaciones a sus pacientes asignados
 * (SecurityConfig exige rol PERSONAL_SALUD; el servicio comprueba además la asignación: si no, 403).
 */
@RestController
@RequestMapping("/api/personal-salud/pacientes/{pacienteId}/recomendaciones")
@RequiredArgsConstructor
public class PersonalSaludRecomendacionController {

    private final RecomendacionService recomendacionService;
    private final UsuarioActual usuarioActual;

    @PostMapping
    public ResponseEntity<RecomendacionResponse> crear(@PathVariable Long pacienteId,
                                                       @Valid @RequestBody RecomendacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recomendacionService.crear(usuarioActual.id(), pacienteId, request));
    }

    /** Lo que este profesional le ha escrito a ese paciente. */
    @GetMapping
    public PaginaResponse<RecomendacionResponse> listar(@PathVariable Long pacienteId,
                                                        @RequestParam(defaultValue = "0") int pagina,
                                                        @RequestParam(defaultValue = "20") int tamano) {
        return recomendacionService.listarDelProfesional(usuarioActual.id(), pacienteId, pagina, tamano);
    }
}
