package org.example.vitabackend.dto.response;

import java.time.LocalDateTime;
import org.example.vitabackend.domain.enums.EstadoTomaMed;

public record TomaMedicamentoResponse(
        Long id, Long medicamentoId, String medicamentoNombre, String dosis,
        LocalDateTime fechaHora, EstadoTomaMed estado
) { }
