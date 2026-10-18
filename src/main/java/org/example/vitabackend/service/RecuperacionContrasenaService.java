package org.example.vitabackend.service;

import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerContrasenaRequest;

/** "Olvidé mi contraseña": se pide un código por correo y luego se cambia la contraseña con él. */
public interface RecuperacionContrasenaService {

    /**
     * Genera y envía un código al correo. Si el correo no está registrado responde 404 ("Este correo
     * no está registrado en VITA"), si la cuenta está inactiva o ya se pidió un código hace menos de
     * un minuto responde 400, para que la persona sepa qué pasó.
     */
    void solicitar(OlvideContrasenaRequest request);

    /** Cambia la contraseña si el código es correcto y no ha vencido; si no, BusinessException (400). */
    void restablecer(RestablecerContrasenaRequest request);
}
