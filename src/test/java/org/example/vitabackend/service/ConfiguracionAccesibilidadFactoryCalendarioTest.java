package org.example.vitabackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.example.vitabackend.domain.enums.TipoDiscapacidad;
import org.example.vitabackend.domain.model.ConfiguracionAccesibilidad;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.dto.request.ConfiguracionAccesibilidadRequest;
import org.junit.jupiter.api.Test;

/** Comprueba valores predeterminados de accesibilidad relevantes para los recordatorios de VITA. */
class ConfiguracionAccesibilidadFactoryCalendarioTest {

    private final ConfiguracionAccesibilidadFactory factory = new ConfiguracionAccesibilidadFactory();

    @Test
    void discapacidadVisualActivaContrasteLectorYAvisoVocalPorDefecto() {
        ConfiguracionAccesibilidad resultado = factory.crear(
                Usuario.builder().id(1L).build(), List.of(TipoDiscapacidad.VISUAL), null);

        assertTrue(resultado.getAltoContraste());
        assertTrue(resultado.getLectorPantalla());
        assertTrue(resultado.getNotificacionesVoz());
        assertTrue(resultado.getTamanoTexto() >= 24);
    }

    @Test
    void discapacidadAuditivaDesactivaSonidoYConservaAvisoVisual() {
        ConfiguracionAccesibilidad resultado = factory.crear(
                Usuario.builder().id(2L).build(), List.of(TipoDiscapacidad.AUDITIVA), null);

        assertFalse(resultado.getNotificacionesSonoras());
        assertTrue(resultado.getNotificacionesVisuales());
    }

    @Test
    void discapacidadCognitivaActivaNavegacionSimpleYTextoAmpliado() {
        ConfiguracionAccesibilidad resultado = factory.crear(
                Usuario.builder().id(3L).build(), List.of(TipoDiscapacidad.COGNITIVA), null);

        assertTrue(resultado.getNavegacionSimple());
        assertTrue(resultado.getNotificacionesVoz());
        assertTrue(resultado.getTamanoTexto() >= 20);
    }

    @Test
    void seleccionExplicitaDelUsuarioPrevaleceSobreLosValoresPorDefecto() {
        ConfiguracionAccesibilidadRequest elegida = new ConfiguracionAccesibilidadRequest(
                false, 18, false, true, false, true, true);

        ConfiguracionAccesibilidad resultado = factory.crear(
                Usuario.builder().id(4L).build(), List.of(TipoDiscapacidad.VISUAL), elegida);

        assertFalse(resultado.getAltoContraste());
        assertEquals(18, resultado.getTamanoTexto());
        assertFalse(resultado.getLectorPantalla());
        assertTrue(resultado.getNavegacionSimple());
        assertFalse(resultado.getNotificacionesVoz());
    }
}
