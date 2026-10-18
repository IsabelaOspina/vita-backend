package org.example.vitabackend.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Inicio de sesión con Google: el token de identidad (ID token) que entrega Google a la app. */
public record GoogleLoginRequest(
        @NotBlank(message = "El token de Google es obligatorio") String idToken) {
}
