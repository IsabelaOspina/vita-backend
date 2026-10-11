package org.example.vitabackend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.example.vitabackend.domain.enums.TipoDiscapacidad;
import org.example.vitabackend.domain.model.ConfiguracionAccesibilidad;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ConfiguracionAccesibilidadRequest;
import org.springframework.stereotype.Component;

/**
 * Construye la configuración de accesibilidad inicial de un usuario:
 * 1) parte de valores base,
 * 2) los ajusta según las discapacidades declaradas,
 * 3) respeta lo que el usuario haya elegido explícitamente en el registro.
 */
@Component
public class ConfiguracionAccesibilidadFactory {

    public ConfiguracionAccesibilidad crear(Usuario usuario,
                                            List<TipoDiscapacidad> tipos,
                                            ConfiguracionAccesibilidadRequest elegida) {

        boolean altoContraste = false;
        int tamanoTexto = 16;
        boolean lectorPantalla = false;
        boolean navegacionSimple = false;
        boolean notificacionesVoz = false;
        boolean notificacionesSonoras = true;
        boolean notificacionesVisuales = true;

        Set<TipoDiscapacidad> declaradas = tipos == null ? Set.of() : new HashSet<>(tipos);

        if (declaradas.contains(TipoDiscapacidad.VISUAL)) {
            lectorPantalla = true;
            altoContraste = true;
            tamanoTexto = Math.max(tamanoTexto, 24);
            notificacionesVoz = true;
        }
        if (declaradas.contains(TipoDiscapacidad.AUDITIVA)) {
            notificacionesSonoras = false;
            notificacionesVisuales = true;
        }
        if (declaradas.contains(TipoDiscapacidad.COGNITIVA)) {
            navegacionSimple = true;
            notificacionesVisuales = true;
            notificacionesVoz = true;
            tamanoTexto = Math.max(tamanoTexto, 20);
        }
        if (declaradas.contains(TipoDiscapacidad.DEL_HABLA)) {
            // No depende de comandos de voz: se refuerzan los avisos visuales
            notificacionesVisuales = true;
        }

        // Lo que el usuario eligió explícitamente tiene prioridad
        if (elegida != null) {
            if (elegida.altoContraste() != null) altoContraste = elegida.altoContraste();
            if (elegida.tamanoTexto() != null) tamanoTexto = elegida.tamanoTexto();
            if (elegida.lectorPantalla() != null) lectorPantalla = elegida.lectorPantalla();
            if (elegida.navegacionSimple() != null) navegacionSimple = elegida.navegacionSimple();
            if (elegida.notificacionesVoz() != null) notificacionesVoz = elegida.notificacionesVoz();
            if (elegida.notificacionesSonoras() != null) notificacionesSonoras = elegida.notificacionesSonoras();
            if (elegida.notificacionesVisuales() != null) notificacionesVisuales = elegida.notificacionesVisuales();
        }

        ConfiguracionAccesibilidad config = new ConfiguracionAccesibilidad();
        config.setAltoContraste(altoContraste);
        config.setTamanoTexto(tamanoTexto);
        config.setLectorPantalla(lectorPantalla);
        config.setNavegacionSimple(navegacionSimple);
        config.setNotificacionesVoz(notificacionesVoz);
        config.setNotificacionesSonoras(notificacionesSonoras);
        config.setNotificacionesVisuales(notificacionesVisuales);
        config.setUsuario(usuario);
        return config;
    }
}
