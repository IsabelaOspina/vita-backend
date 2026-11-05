package org.example.vitabackend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_VERSION = "versionToken";

    @Value("${vita.jwt.secret}")
    private String secret;

    @Value("${vita.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey key;

    /*
     * Validar la configuración JWT al iniciar la aplicación, evita utilizar secretos vacíos o demasiado cortos
     */
    @PostConstruct
    private void inicializar() {

        if(secret == null || secret.isBlank()) {
            throw new IllegalStateException("La variable JWT_SECRET no está configurada");
        }
        
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);

        if(secretBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe contener al menos 32 bytes");
        }

        if (expirationMs <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION debe ser mayor que cero");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generarToken(UserDetails user) {
        if(!(user instanceof UsuarioPrincipal principal)) {
            throw new IllegalArgumentException("El usuario debe ser una instancia de UsuarioPrincipal");
        }
        
        Date ahora = new Date();
        
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_VERSION, principal.versionToken())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token, UserDetails user) {
        try {
            if(!(user instanceof UsuarioPrincipal principal)) {
                return false;
            }

            Claims claims = extraerClaims(token);

            Date expiracion = claims.getExpiration();
            Object versionClaim = claims.get(CLAIM_VERSION);

            if(expiracion == null || !(versionClaim instanceof Number)) {
                return false;
            }

            long versionToken = ((Number) versionClaim).longValue();

            return claims.getSubject().equals(user.getUsername())
                    && expiracion.after(new Date())
                    && principal.versionToken() != null
                    && versionToken == principal.versionToken();
                    
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}