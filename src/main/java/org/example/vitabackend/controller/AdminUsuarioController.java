package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.PersonalRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.example.vitabackend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints exclusivos del administrador
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearConRol(@Valid @RequestBody PersonalRequest request) {
        return usuarioService.crearPersonal(request);
    }
}