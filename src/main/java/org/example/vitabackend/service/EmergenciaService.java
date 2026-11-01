package org.example.vitabackend.service;

import java.util.List;
import org.example.vitabackend.dto.request.SolicitudEmergenciaRequest;
import org.example.vitabackend.dto.response.SolicitudEmergenciaResponse;

public interface EmergenciaService {

    SolicitudEmergenciaResponse solicitar(SolicitudEmergenciaRequest request);

    List<SolicitudEmergenciaResponse> historial();

    SolicitudEmergenciaResponse obtener(Long id);

    SolicitudEmergenciaResponse cancelar(Long id);

    SolicitudEmergenciaResponse marcarAtendida(Long id);
}