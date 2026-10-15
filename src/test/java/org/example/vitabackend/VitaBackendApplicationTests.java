package org.example.vitabackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Comprueba el arranque con configuración de prueba y base H2, sin depender del MySQL local. */
@SpringBootTest
@ActiveProfiles("test")
class VitaBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
