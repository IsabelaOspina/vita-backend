package org.example.vitabackend.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.ConfiguracionAccesibilidadFactory;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crea el administrador del sistema al arrancar la aplicación, si todavía no existe.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfiguracionAccesibilidadFactory configuracionFactory;

    @Value("${vita.admin.correo:}")
    private String correo;

    @Value("${vita.admin.contrasena:}")
    private String contrasena;

    @Value("${vita.admin.nombre:Administrador VITA}")
    private String nombre;

    @Value("${vita.admin.cedula:0000000000}")
    private String cedula;
    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (correo.isBlank() || contrasena.isBlank()) {
            log.warn("No se creó el administrador: configure vita.admin.correo y vita.admin.contrasena");
            return;
        }
        if (contrasena.length() < 8) {
            log.warn("No se creó el administrador: la contraseña debe tener al menos 8 caracteres");
            return;
        }

        // Solo puede haber un administrador: si ya existe uno (con cualquier correo), no se crea otro
        if (usuarioRepository.existsByRol(Rol.ADMIN)) {
            return;
        }
        if (usuarioRepository.existsByCorreo(correo)) {
            log.warn("No se creó el administrador: el correo {} ya pertenece a otra cuenta", correo);
            return;
        }
        if (usuarioRepository.existsByCedula(cedula)) {
            log.warn("No se creó el administrador: la cédula {} ya está registrada", cedula);
            return;
        }

        Usuario admin = Usuario.builder()
                .cedula(cedula)
                .nombre(nombre)
                .correo(correo)
                .contrasena(passwordEncoder.encode(contrasena))
                .activo(true)
                .rol(Rol.ADMIN)
                .build();
        admin.setConfiguracionAccesibilidad(configuracionFactory.crear(admin, List.of(), null));

        usuarioRepository.save(admin);
        log.info("Administrador del sistema creado: {}", correo);
    }
}
