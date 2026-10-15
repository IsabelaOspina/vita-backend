package org.example.vitabackend.security;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Entrega el id de la cuenta que hizo la petición (a partir del token ya validado). */
@Component
@RequiredArgsConstructor
public class UsuarioActual {

    private final UsuarioRepository usuarioRepository;

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        return usuarioRepository.findByCorreo(auth.getName())
                .map(Usuario::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
