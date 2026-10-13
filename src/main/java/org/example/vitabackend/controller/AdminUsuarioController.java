package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.dto.request.PersonalRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.example.vitabackend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints exclusivos del administrador (SecurityConfig exige rol ADMIN para /api/admin/**)
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    /** Registra un profesional de la salud (rol PERSONAL_SALUD). */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearPersonal(@Valid @RequestBody PersonalRequest request) {
        return usuarioService.crearPersonal(request);
    }

    /** Lista cuentas. Con ?rol=PERSONAL_SALUD (o USUARIO / ADMIN) filtra; sin parámetro lista todas. */
    @GetMapping
    public List<UsuarioResponse> listar(@RequestParam(required = false) Rol rol) {
        return rol == null ? usuarioService.listar() : usuarioService.listarPorRol(rol);
    }

    /** Vuelve a activar una cuenta desactivada. */
    @PatchMapping("/{id}/reactivar")
    public UsuarioResponse reactivar(@PathVariable Long id) {
        return usuarioService.reactivar(id);
    }
}
