
package org.example.vitabackend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import org.example.vitabackend.config.InalambriaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class InalambriaSmsServiceTest {

    private InalambriaConfig inalambriaConfig;
    private InalambriaSmsService inalambriaSmsService;
    private MockRestServiceServer servidor;

    private static final String TELEFONO = "+573001234567";
    private static final String MENSAJE = "Alerta de emergencia - VITA.";

    @BeforeEach
    void configurar() {
        inalambriaConfig = new InalambriaConfig();

        RestClient.Builder builder = RestClient.builder();

        servidor = MockRestServiceServer.bindTo(builder).build();

        inalambriaSmsService = new InalambriaSmsService(
                inalambriaConfig,
                builder
        );
    }

    @Test
    void enviarSms_ConApiKeyYTelefonoValidosEnviaLaPeticion() {
        ReflectionTestUtils.setField(
                inalambriaConfig,
                "apiKey",
                "api-key-de-prueba"
        );

        servidor.expect(requestTo(
                        "https://api.inalambria.express/v1/messages/send"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(
                        "Authorization",
                        "Bearer api-key-de-prueba"
                ))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                            "recipients": ["+573001234567"],
                            "content": "Alerta de emergencia - VITA.",
                            "async": false
                        }
                        """))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() ->
                inalambriaSmsService.enviarSms(TELEFONO, MENSAJE)
        );

        servidor.verify();
    }

    @Test
    void enviarSms_SinApiKeyLanzaExcepcion() {
        assertThrows(IllegalStateException.class, () ->
                inalambriaSmsService.enviarSms(TELEFONO, MENSAJE)
        );

        servidor.verify();
    }

    @Test
    void enviarSms_ConTelefonoInvalidoLanzaExcepcion() {
        ReflectionTestUtils.setField(
                inalambriaConfig,
                "apiKey",
                "api-key-de-prueba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                inalambriaSmsService.enviarSms("3001234567", MENSAJE)
        );

        servidor.verify();
    }

    @Test
    void enviarSms_ConTelefonoNullLanzaExcepcion() {
        ReflectionTestUtils.setField(
                inalambriaConfig,
                "apiKey",
                "api-key-de-prueba"
        );

        assertThrows(IllegalArgumentException.class, () ->
                inalambriaSmsService.enviarSms(null, MENSAJE)
        );

        servidor.verify();
    }

    @Test
    void enviarSms_CuandoInalambriaRespondeConErrorLanzaRuntimeException() {
        ReflectionTestUtils.setField(
                inalambriaConfig,
                "apiKey",
                "api-key-de-prueba"
        );

        servidor.expect(requestTo(
                        "https://api.inalambria.express/v1/messages/send"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError());

        RuntimeException excepcion = assertThrows(
                RuntimeException.class,
                () -> inalambriaSmsService.enviarSms(TELEFONO, MENSAJE)
        );

        assertEquals(
                "Inalambria no pudo procesar el envío del SMS.",
                excepcion.getMessage()
        );

        servidor.verify();
    }

    @Test
    void enviarSms_ConTelefonoInternacionalValidoAceptaLaSolicitud() {
        ReflectionTestUtils.setField(
                inalambriaConfig,
                "apiKey",
                "api-key-de-prueba"
        );

        servidor.expect(requestTo(
                        "https://api.inalambria.express/v1/messages/send"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertDoesNotThrow(() ->
                inalambriaSmsService.enviarSms("+573101234567", MENSAJE)
        );

        servidor.verify();
    }
}
