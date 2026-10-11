package org.example.vitabackend.service.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.enums.TipoDiscapacidad;
import org.example.vitabackend.domain.model.ConfiguracionAccesibilidad;
import org.example.vitabackend.domain.model.Discapacidad;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.DiscapacidadRequest;
import org.example.vitabackend.dto.request.UsuarioRequest;
import org.example.vitabackend.dto.response.UsuarioResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.mapper.UsuarioMapper;
import org.example.vitabackend.repository.ContactoEmergenciaRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.ConfiguracionAccesibilidadFactory;
import org.example.vitabackend.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ContactoEmergenciaRepository contactoEmergenciaRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final ConfiguracionAccesibilidadFactory configuracionFactory;

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByCorreo(request.correo())) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByCedula(request.cedula())) {
            throw new BusinessException("Ya existe un usuario con esa cédula");
        }
        validarDiscapacidadesSinRepetir(request.discapacidades());

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setContrasena(passwordEncoder.encode(request.contrasena()));
        usuario.setActivo(true);

        // Contacto de emergencia (opcional): se guarda primero porque el usuario apunta a él
        if (usuario.getContactoEmergencia() != null) {
            usuario.setContactoEmergencia(
                    contactoEmergenciaRepository.save(usuario.getContactoEmergencia()));
        }

        // Discapacidades (opcionales): cada una apunta de vuelta al usuario
        if (usuario.getDiscapacidades() == null) {
            usuario.setDiscapacidades(new ArrayList<>());
        }
        for (Discapacidad d : usuario.getDiscapacidades()) {
            d.setUsuario(usuario);
        }

        // Configuración de accesibilidad: valores por defecto según discapacidades + lo elegido
        List<TipoDiscapacidad> tipos = usuario.getDiscapacidades().stream()
                .map(Discapacidad::getTipo)
                .toList();
        ConfiguracionAccesibilidad configuracion =
                configuracionFactory.crear(usuario, tipos, request.configuracionAccesibilidad());
        usuario.setConfiguracionAccesibilidad(configuracion);

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return usuarioMapper.toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorCorreo(String correo) {
        return usuarioMapper.toResponse(usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado")));
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

    private void validarDiscapacidadesSinRepetir(List<DiscapacidadRequest> discapacidades) {
        if (discapacidades == null) {
            return;
        }
        long distintas = discapacidades.stream().map(DiscapacidadRequest::tipo).distinct().count();
        if (distintas != discapacidades.size()) {
            throw new BusinessException("No repita el mismo tipo de discapacidad");
        }
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + id));
    }
}
