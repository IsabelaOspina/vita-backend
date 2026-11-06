package org.example.vitabackend.exception;

import org.springframework.security.core.AuthenticationException;

public class CredencialesInvalidasException extends AuthenticationException {
    public CredencialesInvalidasException() {
        super("No fue posible iniciar sesión");
    }
}
