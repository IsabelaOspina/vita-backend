package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.GoogleLoginRequest;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);

    /**
     * Inicio de sesión con una cuenta de Google. Solo entra si ya existe una cuenta de VITA con ese
     * correo; si no existe, ResourceNotFoundException (404) para que la app pida registrarse primero.
     */
    AuthResponse loginConGoogle(GoogleLoginRequest request);
}
