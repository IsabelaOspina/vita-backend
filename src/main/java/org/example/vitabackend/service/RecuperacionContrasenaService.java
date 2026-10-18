package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerConGoogleRequest;
import org.example.vitabackend.dto.request.RestablecerContrasenaRequest;

/**
 * "Olvidé mi contraseña", de dos formas:
 * 1. Con un código de 6 dígitos que se envía al correo.
 * 2. Verificando el correo con la cuenta de Google (sin código).
 */
public interface RecuperacionContrasenaService {

    /**
     * Genera y envía un código al correo. Si el correo no está registrado responde 404 ("Este correo
     * no está registrado en VITA"), si la cuenta está inactiva o ya se pidió un código hace menos de
     * un minuto responde 400, para que la persona sepa qué pasó.
     */
    void solicitar(OlvideContrasenaRequest request);

    /** Cambia la contraseña si el código es correcto y no ha vencido; si no, BusinessException (400). */
    void restablecer(RestablecerContrasenaRequest request);

    /**
     * Cambia la contraseña verificando el correo con Google: el token de Google debe ser válido y su
     * correo debe pertenecer a una cuenta activa de VITA.
     */
    void restablecerConGoogle(RestablecerConGoogleRequest request);
}
