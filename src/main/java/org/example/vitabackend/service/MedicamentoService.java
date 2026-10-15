package org.example.vitabackend.service;

import java.util.List;
import org.example.vitabackend.dto.request.MedicamentoRequest;
import org.example.vitabackend.dto.response.MedicamentoResponse;

public interface MedicamentoService {
    MedicamentoResponse crear(String correo, MedicamentoRequest request);
    List<MedicamentoResponse> listar(String correo);
    MedicamentoResponse obtener(String correo, Long id);
    MedicamentoResponse actualizar(String correo, Long id, MedicamentoRequest request);
    void desactivar(String correo, Long id);
}
