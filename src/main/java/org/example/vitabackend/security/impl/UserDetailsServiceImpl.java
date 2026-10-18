package org.example.vitabackend.security.impl;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.security.Credenciales;
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
        // Se traen solo los datos de acceso (una consulta corta): esto se ejecuta en cada petición de la app
        Credenciales usuario = usuarioRepository.findCredencialesByCorreo(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario con email " + email + " no encontrado"));

        return User.builder()
                .username(usuario.correo())
                .password(usuario.contrasena())
                .roles(usuario.rol().name()) // genera la autoridad ROLE_ADMIN o ROLE_USUARIO
                .disabled(!Boolean.TRUE.equals(usuario.activo()))
                .build();
    }
}