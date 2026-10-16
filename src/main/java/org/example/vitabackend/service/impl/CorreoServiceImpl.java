package org.example.vitabackend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.service.CorreoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía los correos por SMTP cuando vita.correo.habilitado=true (y spring.mail.* está configurado).
 * Si no está habilitado (caso normal en desarrollo) NO envía nada: escribe el código en la consola
 * del backend para poder probar el flujo completo sin un servidor de correo.
 */
@Slf4j
@Service
public class CorreoServiceImpl implements CorreoService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean habilitado;
    private final String remitente;

    public CorreoServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                             @Value("${vita.correo.habilitado:false}") boolean habilitado,
                             @Value("${vita.correo.remitente:no-reply@vita.local}") String remitente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remitente = remitente;

        log.info("Correo habilitado: {}", habilitado);
        log.info("JavaMailSender disponible: {}", mailSender.getIfAvailable() != null);
    }

    @Override
    public void enviarCodigoRecuperacion(String correo, String nombre, String codigo, int minutosValidez) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!habilitado || sender == null) {
            log.warn("[DEV] Correo deshabilitado. Código de recuperación para {}: {} (vale {} min)",
                    correo, codigo, minutosValidez);
            return;
        }
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject("VITA: código para restablecer su contraseña");
        mensaje.setText("Hola " + nombre + ",\n\n"
                + "Recibimos una solicitud para restablecer la contraseña de su cuenta de VITA.\n\n"
                + "Su código es: " + codigo + "\n\n"
                + "Vale por " + minutosValidez + " minutos y solo se puede usar una vez.\n"
                + "Si usted no lo pidió, ignore este mensaje: su contraseña no cambiará.\n\n"
                + "Equipo VITA");
        sender.send(mensaje);
    }
}
