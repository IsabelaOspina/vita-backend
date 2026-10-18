package org.example.vitabackend.security;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Verifica el token de identidad (ID token) que Google entrega a la app al iniciar sesión.
 *
 * Se consulta el servicio oficial de Google (tokeninfo), que comprueba la firma y la vigencia del
 * token, y aquí se revisa además que el token fue emitido para VITA (aud = uno de nuestros ID de
 * cliente de OAuth) y que Google ya verificó el correo. Así no se necesita ninguna librería extra.
 *
 * Configuración: vita.google.client-ids (variable GOOGLE_CLIENT_IDS), uno o varios ID de cliente
 * separados por coma. Basta con el ID de cliente "Web": la app Android también pide el token con él.
 */
@Slf4j
@Component
public class GoogleTokenVerifier {

    private static final String TOKENINFO = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";
    private static final List<String> EMISORES = List.of("accounts.google.com", "https://accounts.google.com");
    static final String NO_VERIFICADO = "No pudimos verificar su cuenta de Google. Intente de nuevo";

    private final List<String> clientIds;
    private final RestClient restClient = RestClient.create();

    public GoogleTokenVerifier(@Value("${vita.google.client-ids:}") String clientIds) {
        this.clientIds = Arrays.stream(clientIds.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .toList();
        log.info("Inicio de sesión con Google configurado: {}", !this.clientIds.isEmpty());
    }

    /** Datos de la cuenta de Google ya verificada. */
    public record CuentaGoogle(String correo, String nombre) {
    }

    /**
     * Devuelve el correo (y el nombre) de la cuenta de Google si el token es válido y es para VITA.
     * Si no, lanza BusinessException (400) con un mensaje para el usuario.
     */
    public CuentaGoogle verificar(String idToken) {
        if (clientIds.isEmpty()) {
            throw new BusinessException("El inicio de sesión con Google no está disponible en este momento");
        }

        Map<?, ?> datos;
        try {
            datos = restClient.get().uri(TOKENINFO, idToken).retrieve().body(Map.class);
        } catch (RestClientException e) {
            // Google responde 400 si el token es falso, está mal formado o ya venció
            log.warn("Token de Google rechazado: {}", e.getMessage());
            throw new BusinessException(NO_VERIFICADO);
        }
        if (datos == null) {
            throw new BusinessException(NO_VERIFICADO);
        }

        String audiencia = texto(datos.get("aud"));
        String emisor = texto(datos.get("iss"));
        String correo = texto(datos.get("email"));
        boolean correoVerificado = "true".equalsIgnoreCase(texto(datos.get("email_verified")));

        if (!clientIds.contains(audiencia) || !EMISORES.contains(emisor)) {
            log.warn("Token de Google con audiencia o emisor no válidos: aud={}, iss={}", audiencia, emisor);
            throw new BusinessException(NO_VERIFICADO);
        }
        if (correo.isEmpty() || !correoVerificado) {
            throw new BusinessException("Su cuenta de Google no tiene un correo verificado");
        }
        return new CuentaGoogle(correo.toLowerCase(), texto(datos.get("name")));
    }

    private static String texto(Object valor) {
        return valor == null ? "" : String.valueOf(valor).trim();
    }
}
