package org.example.vitabackend.security;

import org.example.vitabackend.domain.enums.Rol;

/**
 * Lo único que necesita la seguridad para validar un token o un inicio de sesión. Se consulta solo
 * esto (y no la cuenta completa) para que cada petición haga una sola consulta corta a la base de datos.
 */
public record Credenciales(String correo, String contrasena, Rol rol, Boolean activo) {
}
