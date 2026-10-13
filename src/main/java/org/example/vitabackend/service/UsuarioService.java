package org.example.vitabackend.service;

import java.util.List;

import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.dto.request.CambiarContrasenaRequest;
import org.example.vitabackend.dto.request.PersonalRequest;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;

public interface UsuarioService {

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse crearPersonal(PersonalRequest request);

    UsuarioResponse obtener(Long id);

    List<UsuarioResponse> listar();

    List<UsuarioResponse> listarPorRol(Rol rol);

    void desactivar(Long id);

    UsuarioResponse reactivar(Long id);

    void cambiarContrasena(String correo, CambiarContrasenaRequest request);

    UsuarioResponse obtenerPorCorreo(String correo);
}