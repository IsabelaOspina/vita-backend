package org.example.vitabackend.dto.response;

import java.time.LocalDate;

public record UsuarioResponse(
        Long id,
        String cedula,
        String nombre,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String direccion,
        Boolean activo) {
}

