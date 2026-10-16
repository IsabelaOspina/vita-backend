package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerContrasenaRequest;

/** "Olvidé mi contraseña": se pide un código por correo y luego se cambia la contraseña con él. */
public interface RecuperacionContrasenaService {

    /**
     * Genera y envía un código si el correo pertenece a una cuenta activa. Siempre termina igual
     * (exista o no el correo) para no revelar qué correos están registrados.
     */
    void solicitar(OlvideContrasenaRequest request);

    /** Cambia la contraseña si el código es correcto y no ha vencido; si no, BusinessException (400). */
    void restablecer(RestablecerContrasenaRequest request);
}
