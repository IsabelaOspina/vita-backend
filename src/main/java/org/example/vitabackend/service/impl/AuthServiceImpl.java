package org.example.vitabackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.LoginRequest;
import org.example.vitabackend.dto.response.AuthResponse;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.UsuarioMapper;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.JwtService;
import org.example.vitabackend.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

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
}
