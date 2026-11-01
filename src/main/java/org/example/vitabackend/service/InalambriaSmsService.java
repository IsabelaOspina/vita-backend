
package org.example.vitabackend.service;

import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.config.InalambriaConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InalambriaSmsService {

    private static final String URL_API = "https://api.inalambria.express/v1";

    private final InalambriaConfig inalambriaConfig;
    private final RestClient restClient;

    public InalambriaSmsService(
            InalambriaConfig inalambriaConfig,
            RestClient.Builder restClientBuilder) {

        this.inalambriaConfig = inalambriaConfig;

        this.restClient = restClientBuilder
                .baseUrl(URL_API)
                .build();
    }

    public void enviarSms(String telefonoDestino, String mensaje) {

        if (!inalambriaConfig.estaConfigurado()) {
            throw new IllegalStateException(
                    "Inalambria no está configurado. Verifique la API key."
            );
        }

        if (telefonoDestino == null ||
                !telefonoDestino.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException(
                    "El teléfono debe estar en formato internacional, por ejemplo +573001234567."
            );
        }

        Map<String, Object> cuerpo = Map.of(
                "recipients", List.of(telefonoDestino),
                "content", mensaje,
                "async", false
        );

        try {
            restClient.post()
                    .uri("/messages/send")
                    .header("Authorization", "Bearer " + inalambriaConfig.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cuerpo)
                    .retrieve()
                    .body(String.class);

            log.info("Inalambria aceptó la solicitud de envío SMS.");

        } catch (Exception e) {
            log.error("Error al enviar SMS mediante Inalambria: {}", e.getMessage(), e);

            throw new RuntimeException(
                    "Inalambria no pudo procesar el envío del SMS.", e
            );
        }
    }
}
