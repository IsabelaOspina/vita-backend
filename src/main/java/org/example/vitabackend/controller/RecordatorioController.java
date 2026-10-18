package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.RecordatorioRequest;
import org.example.vitabackend.dto.response.RecordatorioResponse;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.example.vitabackend.service.RecordatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** API de recordatorios: la entrega de notificaciones locales se realiza en Expo Go. */
@RestController
@RequiredArgsConstructor
public class RecordatorioController {
    private final RecordatorioService recordatorioService;
    private final HorarioMedicamentoService horarioService;

    @PostMapping("/api/actividades-salud/{actividadId}/recordatorios")
    public ResponseEntity<RecordatorioResponse> crearParaActividad(
            @PathVariable Long actividadId,
            @Valid @RequestBody RecordatorioRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                recordatorioService.crearParaActividad(authentication.getName(), actividadId, request));
    }

    @GetMapping("/api/actividades-salud/{actividadId}/recordatorios")
    public List<RecordatorioResponse> listarDeActividad(
            @PathVariable Long actividadId, Authentication authentication) {
        return recordatorioService.listarDeActividad(authentication.getName(), actividadId);
    }

    /** Agenda unificada para sincronizar próximos recordatorios del usuario con el dispositivo. */
    @GetMapping("/api/recordatorios/agenda")
    public List<RecordatorioResponse> listarAgenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication authentication) {
        // Antes de responder, se generan las tomas de medicamentos que falten en los próximos días
        try {
            horarioService.asegurarHorizonte(authentication.getName());
        } catch (RuntimeException e) {
            // Si dos consultas llegan al tiempo, una puede chocar con las tomas que crea la otra:
            // no impide responder y se completa en la próxima consulta
        }
        return recordatorioService.listarAgenda(authentication.getName(), desde, hasta);
    }

    @DeleteMapping("/api/recordatorios/{id}")
    public RecordatorioResponse cancelar(@PathVariable Long id, Authentication authentication) {
        return recordatorioService.cancelar(authentication.getName(), id);
    }

    /** Reporte informativo desde la app; no certifica que la actividad se haya realizado. */
    @PatchMapping("/api/recordatorios/{id}/enviado")
    public RecordatorioResponse marcarEnviado(@PathVariable Long id, Authentication authentication) {
        return recordatorioService.marcarEnviado(authentication.getName(), id);
    }

    @PatchMapping("/api/recordatorios/{id}/confirmar")
    public RecordatorioResponse confirmar(@PathVariable Long id, Authentication authentication) {
        return recordatorioService.confirmar(authentication.getName(), id);
    }
}
