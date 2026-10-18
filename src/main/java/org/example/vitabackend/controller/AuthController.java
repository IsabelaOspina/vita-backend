package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerContrasenaRequest;
import org.example.vitabackend.dto.response.AuthResponse;
import org.example.vitabackend.dto.response.MensajeResponse;
import org.example.vitabackend.service.AuthService;
import org.example.vitabackend.service.RecuperacionContrasenaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RecuperacionContrasenaService recuperacionService;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /** Paso 1: el usuario escribe su correo y le llega un código de 6 dígitos. */
    @PostMapping("/olvide-contrasena")
    public MensajeResponse olvideContrasena(@Valid @RequestBody OlvideContrasenaRequest request) {
        recuperacionService.solicitar(request);
        // Mismo mensaje exista o no el correo (no se revela qué correos están registrados)
        return MensajeResponse.ok("Si el correo está registrado, le enviamos un código para restablecer su contraseña");
    }

    /** Paso 2: correo + código + nueva contraseña. */
    @PostMapping("/restablecer-contrasena")
    public MensajeResponse restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest request) {
        recuperacionService.restablecer(request);
        return MensajeResponse.ok("Su contraseña se actualizó correctamente. Ya puede iniciar sesión");
    }
}
