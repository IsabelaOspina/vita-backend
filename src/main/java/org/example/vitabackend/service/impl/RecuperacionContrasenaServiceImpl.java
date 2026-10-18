package org.example.vitabackend.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.config.Reloj;
import org.example.vitabackend.domain.model.RecuperacionContrasena;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.OlvideContrasenaRequest;
import org.example.vitabackend.dto.request.RestablecerContrasenaRequest;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.repository.RecuperacionContrasenaRepository;
import org.example.vitabackend.repository.UsuarioRepository;
import org.example.vitabackend.service.CorreoService;
import org.example.vitabackend.service.RecuperacionContrasenaService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacionContrasenaServiceImpl implements RecuperacionContrasenaService {

    static final int MINUTOS_VALIDEZ = 15;
    static final int MAX_INTENTOS = 5;
    static final int SEGUNDOS_ENTRE_SOLICITUDES = 60;
    static final String CODIGO_INVALIDO = "El código es incorrecto o ya venció. Solicite uno nuevo";

    private final UsuarioRepository usuarioRepository;
    private final RecuperacionContrasenaRepository recuperacionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CorreoService correoService;
    private final Reloj reloj;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional
    public void solicitar(OlvideContrasenaRequest request) {
        Optional<Usuario> encontrado = usuarioRepository.findByCorreo(request.correo().trim());
        // Correo desconocido o cuenta inactiva: no se hace nada y se responde igual que si existiera
        if (encontrado.isEmpty() || !Boolean.TRUE.equals(encontrado.get().getActivo())) {
            return;
        }
        Usuario usuario = encontrado.get();
        LocalDateTime ahora = reloj.ahora();

        // Evita que alguien dispare correos sin parar: máximo uno por minuto
        Optional<RecuperacionContrasena> ultimo =
                recuperacionRepository.findFirstByUsuario_IdOrderByIdDesc(usuario.getId());
        if (ultimo.isPresent()
                && ultimo.get().getFechaCreacion().plusSeconds(SEGUNDOS_ENTRE_SOLICITUDES).isAfter(ahora)) {
            return;
        }

        // Un código nuevo anula los anteriores
        for (RecuperacionContrasena anterior : recuperacionRepository.findByUsuario_IdAndUsadoFalse(usuario.getId())) {
            anterior.setUsado(true);
        }

        String codigo = String.format("%06d", random.nextInt(1_000_000));
        recuperacionRepository.save(RecuperacionContrasena.builder()
                .usuario(usuario)
                .codigoHash(passwordEncoder.encode(codigo))
                .fechaCreacion(ahora)
                .fechaExpiracion(ahora.plusMinutes(MINUTOS_VALIDEZ))
                .intentos(0)
                .usado(false)
                .build());

        try {
            correoService.enviarCodigoRecuperacion(usuario.getCorreo(), usuario.getNombre(), codigo, MINUTOS_VALIDEZ);
        } catch (RuntimeException e) {
            // No se le dice al cliente (respondería distinto según el correo); queda en el log
            log.error("No se pudo enviar el código de recuperación a {}", usuario.getCorreo(), e);
        }
    }

    /**
     * noRollbackFor: cuando el código es incorrecto se lanza BusinessException, pero el aumento
     * del contador de intentos SÍ debe guardarse (si no, se podrían probar códigos sin límite).
     */
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public void restablecer(RestablecerContrasenaRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.correo().trim())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new BusinessException(CODIGO_INVALIDO));

        RecuperacionContrasena recuperacion = recuperacionRepository
                .findFirstByUsuario_IdAndUsadoFalseOrderByIdDesc(usuario.getId())
                .orElseThrow(() -> new BusinessException(CODIGO_INVALIDO));

        if (recuperacion.getFechaExpiracion().isBefore(reloj.ahora())) {
            recuperacion.setUsado(true);
            throw new BusinessException(CODIGO_INVALIDO);
        }
        if (!passwordEncoder.matches(request.codigo(), recuperacion.getCodigoHash())) {
            recuperacion.setIntentos(recuperacion.getIntentos() + 1);
            if (recuperacion.getIntentos() >= MAX_INTENTOS) {
                recuperacion.setUsado(true); // demasiados intentos: hay que pedir otro código
            }
            throw new BusinessException(CODIGO_INVALIDO);
        }

        usuario.setContrasena(passwordEncoder.encode(request.contrasenaNueva()));
        recuperacion.setUsado(true);
    }
}
