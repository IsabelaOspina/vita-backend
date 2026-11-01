package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.ContactoEmergenciaRequest;
import org.example.vitabackend.dto.response.ContactoEmergenciaResponse;

public interface ContactoEmergenciaService {

    ContactoEmergenciaResponse obtener();

    ContactoEmergenciaResponse guardar(ContactoEmergenciaRequest request);

    void eliminar();
}