package org.example.vitabackend.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRequestConsentimientoTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    private UsuarioRequest crearRequest(
            Boolean aceptaTratamientoDatosPersonales) {

        return new UsuarioRequest(
                "12345678",
                "Ana Maria",
                "ana@vita.com",
                "VitaSegura1!",
                "3001234567",
                LocalDate.of(1995, 1, 1),
                "Armenia",
                null,
                List.of(),
                null,
                aceptaTratamientoDatosPersonales
        );
    }

    @Test
    void aceptaRegistroCuandoAceptaTratamientoDatosPersonales() {
        UsuarioRequest request = crearRequest(true);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaRegistroCuandoNoAceptaTratamientoDatosPersonales() {
        UsuarioRequest request = crearRequest(false);

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaRegistroCuandoNoIndicaAceptacion() {
        UsuarioRequest request = crearRequest(null);

        assertFalse(validator.validate(request).isEmpty());
    }
}
