package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.example.vitabackend.dto.request.SolicitudEmergenciaRequest;
import org.example.vitabackend.dto.response.SolicitudEmergenciaResponse;
import org.example.vitabackend.service.EmergenciaService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergencias")
@RequiredArgsConstructor
public class EmergenciaController {

    private final EmergenciaService emergenciaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudEmergenciaResponse solicitar(
            @Valid @RequestBody SolicitudEmergenciaRequest request
    ) {
        return emergenciaService.solicitar(request);
    }

    @GetMapping
    public List<SolicitudEmergenciaResponse> historial() {
        return emergenciaService.historial();
    }

    @GetMapping("/{id}")
    public SolicitudEmergenciaResponse obtener(
            @PathVariable Long id
    ) {
        return emergenciaService.obtener(id);
    }

    @PatchMapping("/{id}/cancelar")
    public SolicitudEmergenciaResponse cancelar(
            @PathVariable Long id
    ) {
        return emergenciaService.cancelar(id);
    }

    @PatchMapping("/{id}/atendida")
    public SolicitudEmergenciaResponse marcarAtendida(
            @PathVariable Long id
    ) {
        return emergenciaService.marcarAtendida(id);
    }
}