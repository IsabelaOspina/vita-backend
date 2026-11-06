package org.example.vitabackend.validation;

import java.nio.charset.StandardCharsets;

public final class PoliticaContrasena {
    private static final int LONGITUD_MINIMA = 8;
    private static final int LONGITUD_MAXIMA_BCRYPT_BYTES = 72;

    private PoliticaContrasena() {
    }

    public static boolean cumple(String contrasena) {
        if (contrasena == null) {
            return false;
        }

        int longitud = contrasena.codePointCount(0, contrasena.length());

        if (longitud < LONGITUD_MINIMA) {
            return false;
        }

        int bytesUtf8 = contrasena.getBytes(StandardCharsets.UTF_8).length;

        if (bytesUtf8 > LONGITUD_MAXIMA_BCRYPT_BYTES) {
            return false;
        }

        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneEspecial = false;

        for (int i = 0; i < contrasena.length();) {
            int caracter = contrasena.codePointAt(i);

            if (Character.isUpperCase(caracter)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(caracter)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(caracter)) {
                tieneNumero = true;
            } else if (!Character.isWhitespace(caracter)) {
                tieneEspecial = true;
            }

            i += Character.charCount(caracter);
        }

        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneEspecial;
    }
}