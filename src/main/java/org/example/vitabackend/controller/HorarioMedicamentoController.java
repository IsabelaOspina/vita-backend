package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.HorarioMedicamentoRequest;
import org.example.vitabackend.dto.response.HorarioMedicamentoResponse;
import org.example.vitabackend.dto.response.TomaMedicamentoResponse;
import org.example.vitabackend.service.HorarioMedicamentoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicamentos/{medicamentoId}/horarios")
@RequiredArgsConstructor
public class HorarioMedicamentoController {
    private final HorarioMedicamentoService service;

    @PostMapping
    public ResponseEntity<HorarioMedicamentoResponse> crear(@PathVariable Long medicamentoId,
            @Valid @RequestBody HorarioMedicamentoRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(auth.getName(), medicamentoId, request));
    }
    @GetMapping
    public List<HorarioMedicamentoResponse> listar(@PathVariable Long medicamentoId, Authentication auth) {
        return service.listar(auth.getName(), medicamentoId);
    }
    @DeleteMapping("/{horarioId}")
    public ResponseEntity<Void> cancelar(@PathVariable Long medicamentoId, @PathVariable Long horarioId,
                                         Authentication auth) {
        service.cancelar(auth.getName(), medicamentoId, horarioId); return ResponseEntity.noContent().build();
    }
    @GetMapping("/{horarioId}/tomas")
    public List<TomaMedicamentoResponse> listarTomas(@PathVariable Long medicamentoId, @PathVariable Long horarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Authentication auth) {
        return service.listarTomas(auth.getName(), medicamentoId, horarioId, desde, hasta);
    }
}
