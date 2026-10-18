package org.example.vitabackend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.dto.request.GoogleLoginRequest;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerConGoogleRequest;
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

    /** Inicio de sesión con Google: solo para correos que ya tienen cuenta en VITA (si no, 404). */
    @PostMapping("/google")
    public AuthResponse loginConGoogle(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.loginConGoogle(request);
    }

    /**
     * Paso 1: el usuario escribe su correo y le llega un código de 6 dígitos.
     * Si el correo no está registrado responde 404; si la cuenta está inactiva o pidió otro código
     * hace menos de un minuto, 400.
     */
    @PostMapping("/olvide-contrasena")
    public MensajeResponse olvideContrasena(@Valid @RequestBody OlvideContrasenaRequest request) {
        recuperacionService.solicitar(request);
        return MensajeResponse.ok("Le enviamos un código de 6 dígitos a su correo para restablecer su contraseña");
    }

    /** Paso 2: correo + código + nueva contraseña. */
    @PostMapping("/restablecer-contrasena")
    public MensajeResponse restablecerContrasena(@Valid @RequestBody RestablecerContrasenaRequest request) {
        recuperacionService.restablecer(request);
        return MensajeResponse.ok("Su contraseña se actualizó correctamente. Ya puede iniciar sesión");
    }

    /** Olvidé mi contraseña, verificando el correo con Google (sin código): token de Google + nueva contraseña. */
    @PostMapping("/restablecer-contrasena-google")
    public MensajeResponse restablecerConGoogle(@Valid @RequestBody RestablecerConGoogleRequest request) {
        recuperacionService.restablecerConGoogle(request);
        return MensajeResponse.ok("Su contraseña se actualizó correctamente. Ya puede iniciar sesión");
    }
}
