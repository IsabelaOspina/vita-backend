package org.example.vitabackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.vitabackend.domain.model.ContactoEmergencia;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ContactoEmergenciaRequest;
import org.example.vitabackend.dto.response.ContactoEmergenciaResponse;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.exception.ResourceNotFoundException;
import org.example.vitabackend.repository.ContactoEmergenciaRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.security.UsuarioActual;
import org.example.vitabackend.service.ContactoEmergenciaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactoEmergenciaServiceImpl implements ContactoEmergenciaService {

    private final UsuarioRepository usuarioRepository;
    private final ContactoEmergenciaRepository contactoRepository;
    private final UsuarioActual usuarioActual;

    @Override
    @Transactional(readOnly = true)
    public ContactoEmergenciaResponse obtener() {

        Usuario usuario = buscarUsuario();

        if (usuario.getContactoEmergencia() == null) {
            throw new ResourceNotFoundException(
                    "No tienes un contacto de emergencia registrado"
            );
        }

        return convertirRespuesta(usuario.getContactoEmergencia());
    }

    @Override
    @Transactional
    public ContactoEmergenciaResponse guardar(ContactoEmergenciaRequest request) {

        Usuario usuario = buscarUsuario();

        ContactoEmergencia contacto = usuario.getContactoEmergencia();

        if (contacto == null) {

            contacto = ContactoEmergencia.builder()
                    .nombre(request.nombre().trim())
                    .telefono(formatearTelefono(request.telefono()))
                    .parentesco(request.parentesco().trim())
                    .build();

        } else {

            contacto.setNombre(request.nombre().trim());
            contacto.setTelefono(formatearTelefono(request.telefono()));
            contacto.setParentesco(request.parentesco().trim());
        }

        contacto = contactoRepository.save(contacto);

        usuario.setContactoEmergencia(contacto);
        usuarioRepository.save(usuario);

        return convertirRespuesta(contacto);
    }

    @Override
    @Transactional
    public void eliminar() {

        Usuario usuario = buscarUsuario();

        ContactoEmergencia contacto = usuario.getContactoEmergencia();

        if (contacto == null) {
            throw new ResourceNotFoundException(
                    "No tienes un contacto de emergencia registrado"
            );
        }

        // Conservamos el registro del contacto porque puede estar asociado
        // a solicitudes de emergencia anteriores.
        usuario.setContactoEmergencia(null);
        usuarioRepository.save(usuario);
    }

    private Usuario buscarUsuario() {

        return usuarioRepository.findById(usuarioActual.id())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"
                ));
    }

    private String formatearTelefono(String telefono) {

        String numero = telefono.replaceAll("\\s+", "");

        if (numero.startsWith("+57") && numero.length() == 13) {
            return numero;
        }

        if (numero.length() == 10) {
            return "+57" + numero;
        }

        throw new BusinessException(
                "El teléfono debe ser un número colombiano de 10 dígitos"
        );
    }

    private ContactoEmergenciaResponse convertirRespuesta(
            ContactoEmergencia contacto
    ) {

        return new ContactoEmergenciaResponse(
                contacto.getId(),
                contacto.getNombre(),
                contacto.getTelefono(),
                contacto.getParentesco()
        );
    }
}