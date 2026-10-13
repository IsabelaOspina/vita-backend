package org.example.vitabackend.security;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión (o su sesión expiró)"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta acción")))
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        // Cada usuario puede ver su propio perfil
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                        // Cada paciente ve a sus profesionales; cada profesional ve a sus pacientes
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/me/personal-salud").hasRole("USUARIO")
                        .requestMatchers("/api/personal-salud/**").hasRole("PERSONAL_SALUD")
                        // Administración de cuentas: solo ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/usuarios/*").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Misma estructura que GlobalExceptionHandler, para que el frontend siempre lea "mensaje"
    private void escribirError(HttpServletResponse res, HttpStatus status, String mensaje) throws IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":"
                + status.value() + ",\"mensaje\":\"" + mensaje + "\"}");
    }
}
