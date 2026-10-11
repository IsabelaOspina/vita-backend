package org.example.vitabackend.dto.response;

public record AuthResponse(
        String token,
        String tipo,
        UsuarioResponse usuario) {
}