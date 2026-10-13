package org.example.vitabackend.dto.response;

import java.time.LocalDate;
import java.util.List;
import org.example.vitabackend.domain.enums.Rol;

public record UsuarioResponse(
        Long id,
        String cedula,
        String nombre,
        String correo,
        String telefono,
        LocalDate fechaNacimiento,
        String direccion,
        Boolean activo,
        Rol rol,
        ContactoEmergenciaResponse contactoEmergencia,
        List<DiscapacidadResponse> discapacidades,
        ConfiguracionAccesibilidadResponse configuracionAccesibilidad,
        PersonalSaludResponse personalSalud) {
}
