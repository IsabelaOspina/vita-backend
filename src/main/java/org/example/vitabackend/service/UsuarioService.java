package org.example.vitabackend.service;

import java.util.List;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse obtener(Long id);

    List<UsuarioResponse> listar();

    void desactivar(Long id);

    UsuarioResponse obtenerPorCorreo(String correo);
}