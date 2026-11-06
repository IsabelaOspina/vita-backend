package org.example.vitabackend.service.impl;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.response.AuthResponse;

import org.example.vitabackend.exception.CuentaBloqueadaException;
import org.example.vitabackend.exception.CredencialesInvalidasException;

import org.example.vitabackend.mapper.UsuarioMapper;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.JwtService;
import org.example.vitabackend.service.AuthService;
import org.example.vitabackend.config.Reloj;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_INTENTOS = 3;
    private static final int NIVEL_MAXIMO_BLOQUEO = 2;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final Reloj reloj;

    /**    
     * noRollbackFor es necesario porque los intentos fallidos deben persistirse aunque se devuelva una excepción de autenticación
     */
    @Override
    @Transactional(noRollbackFor = AuthenticationException.class)
    public AuthResponse login(LoginRequest request) {
        String correo = request.correo().trim();

        LocalDateTime ahora = reloj.ahora();

        // Recuperamos la cuenta y bloqueamos su fila en MySQL
        Usuario usuario = usuarioRepository.findByCorreoParaActualizar(correo)
                .orElse(null);

        // Correo inexistente: devolver el mismo error
        if(usuario == null) {
            //Ejecutamos la autenticación nromal
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(correo, request.contrasena()));
            } catch (AuthenticationException ignored) {
                // No se revela si el correo está registrado
            }
            throw new CredencialesInvalidasException();
        }

        // Una cuenta inactiva recibe el mismo mensaje genérico
        if(!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new CredencialesInvalidasException();
        }

        // Comprobar si todavía está vigente el bloqueo
        LocalDateTime bloqueadoHasta = usuario.getBloqueadoHasta();

        if(bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta)) {
            long segundosRestantes = Math.max(1, Duration.between(ahora, bloqueadoHasta).getSeconds());

            throw new CuentaBloqueadaException(segundosRestantes);
        }

        // Si el bloqueo ya venció, habilitamos los siguientes intentos
        if(bloqueadoHasta != null) {
            usuario.setBloqueadoHasta(null);
        }

        // Validamos las credenciales mediante Spring Security
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, request.contrasena()));
        } catch (BadCredentialsException e) {
            registrarIntentoFallido(usuario, ahora);

            // registrarIntentoFallido siempre lanza una excepción
            throw new IllegalStateException("No se pudo registrar el intento fallido");
        }

        // Si el inicio de sesión fue exitoso, reiniciamos todos los contadores
        usuario.setIntentosFallidosLogin(0);
        usuario.setNivelBloqueoLogin(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);

        // Generamos el JWT con la versión vigente del usuario
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(token, "Bearer", usuarioMapper.toResponse(usuario));
    }

    /**
     * Registrar un error de autenticación y aplica el bloqueo correspondiente cuando se completan tres intentos
     */
    private void registrarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        int intentos = usuario.getIntentosFallidosLogin() == null ? 0 : usuario.getIntentosFallidosLogin();

        int nivel = usuario.getNivelBloqueoLogin() == null ? 0 : usuario.getNivelBloqueoLogin();

        intentos++;

        // Si no se completan los tres intentos, solo se incrementa el contador
        if(intentos < MAX_INTENTOS) {
            usuario.setIntentosFallidosLogin(intentos);
            usuarioRepository.save(usuario);

            throw new CredencialesInvalidasException();
        }

        // Determinamos la duración del siguiente bloqueo
        nivel = Math.max(0, nivel);

        long minutosBloqueo = switch (Math.min(nivel, NIVEL_MAXIMO_BLOQUEO)) {
            case 0 -> 1;
            case 1 -> 5;
            default -> 30;
        };

        // Reiniciamos el contador para los siguientes intentos
        usuario.setIntentosFallidosLogin(0);

        // Incrementamos el nivel de bloqueo hasta un máximo de 2
        usuario.setNivelBloqueoLogin(Math.min(nivel + 1, NIVEL_MAXIMO_BLOQUEO));

        // Registramos hasta cuándo estará bloqueada la cuenta
        usuario.setBloqueadoHasta(ahora.plusMinutes(minutosBloqueo));

        usuarioRepository.save(usuario);

        // Informamos que se ha aplicado el bloqueo
        throw new CuentaBloqueadaException(minutosBloqueo * 60);
    }
}
