package org.example.vitabackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.GoogleLoginRequest;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.response.AuthResponse;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.UsuarioMapper;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.GoogleTokenVerifier;
import org.example.vitabackend.security.JwtService;
import org.example.vitabackend.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    static final String SIN_CUENTA_GOOGLE = "Este correo no tiene cuenta en VITA. Regístrese primero";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserDetailsService userDetailsService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Lanza BadCredentialsException / DisabledException si algo falla
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.contrasena()));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtService.generarToken(userDetails);

        Usuario usuario = usuarioRepository.findByCorreo(request.correo())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return new AuthResponse(token, "Bearer", usuarioMapper.toResponse(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse loginConGoogle(GoogleLoginRequest request) {
        // 400 si el token de Google no es válido o no es para VITA
        GoogleTokenVerifier.CuentaGoogle cuenta = googleTokenVerifier.verificar(request.idToken());

        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(cuenta.correo())
                .orElseThrow(() -> new ResourceNotFoundException(SIN_CUENTA_GOOGLE));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            // GlobalExceptionHandler la convierte en 401 "El usuario está inactivo", igual que el login normal
            throw new DisabledException("El usuario está inactivo");
        }

        // El token se genera igual que en el login con contraseña (el "username" es el correo guardado)
        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getCorreo());
        String token = jwtService.generarToken(userDetails);
        return new AuthResponse(token, "Bearer", usuarioMapper.toResponse(usuario));
    }
}
