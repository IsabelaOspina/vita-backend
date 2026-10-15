package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.ActividadSaludRequest;
import org.example.vitabackend.dto.response.ActividadSaludResponse;
import org.example.vitabackend.service.ActividadSaludService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** API REST del calendario de actividades de salud del usuario autenticado. */
@RestController
@RequestMapping("/api/actividades-salud")
@RequiredArgsConstructor
public class ActividadSaludController {

    private final ActividadSaludService actividadSaludService;

    @PostMapping
    public ResponseEntity<ActividadSaludResponse> crear(
            @Valid @RequestBody ActividadSaludRequest request,
            Authentication authentication) {
        ActividadSaludResponse response = actividadSaludService.crear(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ActividadSaludResponse> listar(Authentication authentication) {
        return actividadSaludService.listar(authentication.getName());
    }

    /**
     * Consulta un intervalo inclusivo. El cliente puede construir intervalos de día, semana o mes.
     * Formato esperado: yyyy-MM-dd'T'HH:mm:ss.
     */
    @GetMapping("/rango")
    public List<ActividadSaludResponse> listarPorRango(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication authentication) {
        return actividadSaludService.listarPorRango(authentication.getName(), desde, hasta);
    }

    @GetMapping("/{id}")
    public ActividadSaludResponse obtener(@PathVariable Long id, Authentication authentication) {
        return actividadSaludService.obtener(authentication.getName(), id);
    }

    @PutMapping("/{id}")
    public ActividadSaludResponse actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActividadSaludRequest request,
            Authentication authentication) {
        return actividadSaludService.actualizar(authentication.getName(), id, request);
    }

    @PatchMapping("/{id}/completar")
    public ActividadSaludResponse completar(@PathVariable Long id, Authentication authentication) {
        return actividadSaludService.completar(authentication.getName(), id);
    }

    /** Cancelación lógica: conserva el registro para mantener trazabilidad. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id, Authentication authentication) {
        actividadSaludService.cancelar(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
