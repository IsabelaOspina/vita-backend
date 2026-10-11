package org.example.vitabackend.dto.response;

import java.time.LocalDate;
import java.util.List;

public record UsuarioResponse(
        Long id,
        String cedula,
        String nombre,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String direccion,
        Boolean activo,
        ContactoEmergenciaResponse contactoEmergencia,
        List<DiscapacidadResponse> discapacidades,
        ConfiguracionAccesibilidadResponse configuracionAccesibilidad) {
}
