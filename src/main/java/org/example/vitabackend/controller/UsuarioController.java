package org.example.vitabackend.controller;
import org.springframework.security.core.Authentication;
import org.example.vitabackend.dto.request.CambiarContrasenaRequest;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.AsignacionResponse;
import org.example.vitabackend.service.AsignacionService;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.example.vitabackend.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AsignacionService asignacionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(Authentication authentication) {
        return usuarioService.obtenerPorCorreo(authentication.getName());
    }

    /** Profesionales de la salud asignados al paciente que inició sesión (solo rol USUARIO). */
    @GetMapping("/me/personal-salud")
    public List<AsignacionResponse> misProfesionales(Authentication authentication) {
        Long miId = usuarioService.obtenerPorCorreo(authentication.getName()).id();
        return asignacionService.profesionalesDe(miId);
    }

    /** Cambia la contraseña de quien tiene la sesión iniciada (paciente, personal de salud o admin). */
    @PatchMapping("/me/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarContrasena(Authentication authentication,
                                  @Valid @RequestBody CambiarContrasenaRequest request) {
        usuarioService.cambiarContrasena(authentication.getName(), request);
    }

    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
    }
}
