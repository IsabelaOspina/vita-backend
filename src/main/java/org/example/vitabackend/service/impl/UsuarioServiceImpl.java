package org.example.vitabackend.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.UsuarioMapper;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByCedula(request.cedula())) {
            throw new BusinessException("Ya existe un usuario con esa cédula");
        }
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setActivo(true);
        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return usuarioMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(usuarioMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(false);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}