package org.example.vitabackend.dto.response;

public record MedicamentoResponse(
        Long id, String nombre, String dosis, String viaAdministracion, Boolean activo
) { }


