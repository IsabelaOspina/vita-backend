package org.example.vitabackend.service;

/** Envío de correos de VITA. */
public interface CorreoService {

    void enviarCodigoRecuperacion(String correo, String nombre, String codigo, int minutosValidez);
}
