package org.example.vitabackend.exception;

import org.springframework.security.core.AuthenticationException;

public class CuentaBloqueadaException extends AuthenticationException {
    
    private final long segundosRestantes;

    public CuentaBloqueadaException(long segundosRestantes) {
        super(crearMensaje(segundosRestantes));
        this.segundosRestantes = segundosRestantes;
    }

    public long getSegundosRestantes() {
        return segundosRestantes;
    }

    private static String crearMensaje(long segundos) {
        long minutos = (segundos + 59) / 60;

        if(minutos <= 1) {
            return "El inicio de sesión está bloqueado. "
                    + "Debes esperar aproximadamente 1 minuto para intentarlo nuevamente. ";
        }

        return "El inicio de sesión está bloqueado. "
                + "Debes esperar aproximadamente " + minutos + " minutos para intentarlo nuevamento";
    }
}