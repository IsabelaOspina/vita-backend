package org.example.vitabackend.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.vitabackend.exception.BusinessException;
import org.example.vitabackend.service.CorreoService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envía los correos por SMTP cuando vita.correo.habilitado=true (y spring.mail.* está configurado).
 * Si no está habilitado (caso normal en desarrollo) NO envía nada: escribe el código en la consola
 * del backend para poder probar el flujo completo sin un servidor de correo.
 *
 * En producción (perfil prod) no se puede fingir el envío: si el correo no está habilitado o el
 * servidor de correo falla, se lanza BusinessException para que la persona sepa que el código no
 * llegó (antes la app decía "Le enviamos un código" aunque el correo nunca saliera).
 */
@Slf4j
@Service
public class CorreoServiceImpl implements CorreoService {

    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean habilitado;
    private final String remitente;
    private final boolean produccion;

    static final String SIN_CORREO = "En este momento no podemos enviar correos. Intente más tarde o comuníquese con el administrador";
    static final String FALLO_ENVIO = "No pudimos enviar el correo con el código. Intente de nuevo en unos minutos";

    public CorreoServiceImpl(ObjectProvider<JavaMailSender> mailSender,
                             @Value("${vita.correo.habilitado:false}") boolean habilitado,
                             @Value("${vita.correo.remitente:no-reply@vita.local}") String remitente,
                             @Value("${spring.profiles.active:}") String perfiles) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remitente = remitente;
        this.produccion = perfiles.contains("prod");

        log.info("Correo habilitado: {}", habilitado);
        log.info("JavaMailSender disponible: {}", mailSender.getIfAvailable() != null);
        if (produccion && !habilitado) {
            log.error("El correo NO está habilitado en producción: defina CORREO_HABILITADO=true y MAIL_PASSWORD "
                    + "en Cloud Run, o los códigos para restablecer la contraseña no llegarán");
        }
    }

    @Override
    public void enviarCodigoRecuperacion(String correo, String nombre, String codigo, int minutosValidez) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (!habilitado || sender == null) {
            if (produccion) {
                log.error("No se envió el código de recuperación a {}: el correo no está habilitado", correo);
                throw new BusinessException(SIN_CORREO);
            }
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
        try {
            sender.send(mensaje);
            log.info("Código de recuperación enviado a {}", correo);
        } catch (MailException e) {
            // Por ejemplo: contraseña de aplicación de Gmail incorrecta o servidor de correo sin respuesta
            log.error("No se pudo enviar el código de recuperación a {}", correo, e);
            throw new BusinessException(FALLO_ENVIO);
        }
    }
}