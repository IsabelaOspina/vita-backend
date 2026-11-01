package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.ContactoEmergenciaRequest;
import org.example.vitabackend.dto.response.ContactoEmergenciaResponse;
import org.example.vitabackend.service.ContactoEmergenciaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/emergencias/contacto")
@RequiredArgsConstructor
public class ContactoEmergenciaController {

    private final ContactoEmergenciaService contactoService;

    @GetMapping
    public ContactoEmergenciaResponse obtener() {
        return contactoService.obtener();
    }

    @PutMapping
    public ContactoEmergenciaResponse guardar(
            @Valid @RequestBody ContactoEmergenciaRequest request
    ) {
        return contactoService.guardar(request);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar() {
        contactoService.eliminar();
    }
}