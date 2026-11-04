package org.example.vitabackend.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class InalambriaConfig {

    @Value("${inalambria.api-key:}")
    private String apiKey;

    public boolean estaConfigurado() {
        return apiKey != null && !apiKey.isBlank();
    }
}
