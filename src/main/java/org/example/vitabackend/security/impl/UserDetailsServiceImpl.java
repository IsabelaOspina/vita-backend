package org.example.vitabackend.security.impl;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    // Spring Security llama a este metodo al autenticar; aquí el "username" es el correo.
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con email " + email + " no encontrado"));

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name()) // genera la autoridad ROLE_ADMIN o ROLE_USUARIO
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}