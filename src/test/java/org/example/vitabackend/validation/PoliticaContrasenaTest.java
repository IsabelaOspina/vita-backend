package org.example.vitabackend.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PoliticaContrasenaTest {

    @Test
    void aceptaContrasenaQueCumpleTodasLasReglas() {
        assertTrue(PoliticaContrasena.cumple("VitaSegura1!"));
    }

    @Test
    void aceptaLetrasAcentuadas() {
        assertTrue(PoliticaContrasena.cumple("Ábcdef1!"));
    }

    @Test
    void rechazaContrasenaSinMayuscula() {
        assertFalse(PoliticaContrasena.cumple("vitasegura1!"));
    }

    @Test
    void rechazaContrasenaSinMinuscula() {
        assertFalse(PoliticaContrasena.cumple("VITASEGURA1!"));
    }

    @Test
    void rechazaContrasenaSinNumero() {
        assertFalse(PoliticaContrasena.cumple("VitaSegura!"));
    }

    @Test
    void rechazaContrasenaSinCaracterEspecial() {
        assertFalse(PoliticaContrasena.cumple("VitaSegura12"));
    }

    @Test
    void rechazaContrasenaConMenosDeOchoCaracteres() {
        assertFalse(PoliticaContrasena.cumple("Vi1!"));
    }

    @Test
    void aceptaContrasenaDeExactamente72Bytes() {
        String contrasena = "Aa1!" + "x".repeat(68);

        assertEquals(72, contrasena.getBytes(
                java.nio.charset.StandardCharsets.UTF_8).length);

        assertTrue(PoliticaContrasena.cumple(contrasena));
    }

    @Test
    void rechazaContrasenaQueSuperaLos72Bytes() {
        String contrasena = "Aa1!" + "x".repeat(69);

        assertFalse(PoliticaContrasena.cumple(contrasena));
    }

    @Test
    void rechazaValorNulo() {
        assertFalse(PoliticaContrasena.cumple(null));
    }
}
