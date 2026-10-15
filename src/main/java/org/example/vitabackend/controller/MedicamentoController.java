package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.MedicamentoRequest;
import org.example.vitabackend.dto.response.MedicamentoResponse;
import org.example.vitabackend.service.MedicamentoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {
    private final MedicamentoService service;

    @PostMapping
    public ResponseEntity<MedicamentoResponse> crear(@Valid @RequestBody MedicamentoRequest request,
                                                      Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(auth.getName(), request));
    }
    @GetMapping
    public List<MedicamentoResponse> listar(Authentication auth) { return service.listar(auth.getName()); }
    @GetMapping("/{id}")
    public MedicamentoResponse obtener(@PathVariable Long id, Authentication auth) { return service.obtener(auth.getName(), id); }
    @PutMapping("/{id}")
    public MedicamentoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedicamentoRequest request,
                                           Authentication auth) {
        return service.actualizar(auth.getName(), id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id, Authentication auth) {
        service.desactivar(auth.getName(), id); return ResponseEntity.noContent().build();
    }
}
