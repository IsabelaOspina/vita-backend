package org.example.vitabackend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.NivelIntensidad;
import org.example.vitabackend.domain.enums.Tendencia;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;
import org.example.vitabackend.service.SintomaAnalisis.Registro;
import org.junit.jupiter.api.Test;

/** Cálculos del seguimiento de síntomas: no necesita Spring ni base de datos. */
class SintomaAnalisisTest {

    private static final LocalDate DESDE = LocalDate.of(2026, 10, 1);
    private static final LocalDate HASTA = LocalDate.of(2026, 10, 14);

    private static Registro registro(String fechaHora, String tipo, int intensidad) {
        return new Registro(LocalDateTime.parse(fechaHora), tipo, intensidad);
    }

    private static List<Registro> dolorDeCabezaQueEmpeora() {
        return List.of(
                registro("2026-10-01T08:00:00", "dolor de cabeza", 3),
                registro("2026-10-01T20:00:00", "Dolor de cabeza", 5),
                registro("2026-10-05T08:00:00", "dolor de cabeza", 4),
                registro("2026-10-10T08:00:00", "dolor de cabeza", 8),
                registro("2026-10-12T08:00:00", "dolor de cabeza", 9));
    }

    @Test
    void nivel_ClasificaLeveModeradaYSevera() {
        assertEquals(NivelIntensidad.LEVE, SintomaAnalisis.nivel(1));
        assertEquals(NivelIntensidad.LEVE, SintomaAnalisis.nivel(3));
        assertEquals(NivelIntensidad.MODERADA, SintomaAnalisis.nivel(4));
        assertEquals(NivelIntensidad.MODERADA, SintomaAnalisis.nivel(6));
        assertEquals(NivelIntensidad.SEVERA, SintomaAnalisis.nivel(7));
        assertEquals(NivelIntensidad.SEVERA, SintomaAnalisis.nivel(10));
    }

    @Test
    void normalizarTipo_QuitaEspaciosSobrantes() {
        assertEquals("dolor de cabeza", SintomaAnalisis.normalizarTipo("  dolor   de  cabeza "));
        assertEquals("", SintomaAnalisis.normalizarTipo(null));
        assertEquals("dolor de cabeza", SintomaAnalisis.claveTipo(" Dolor  DE Cabeza"));
    }

    @Test
    void textoRegistro_ArmaLaFraseParaLeerEnVozAlta() {
        String texto = SintomaAnalisis.textoRegistro("dolor de cabeza", 7,
                LocalDateTime.of(2026, 10, 14, 15, 30), " me duele mucho ");

        assertEquals("dolor de cabeza, intensidad 7 de 10, severa. "
                + "El 14 de octubre de 2026 a las 15:30. Observaciones: me duele mucho", texto);
    }

    @Test
    void textoRegistro_SinObservacionesNoLasMenciona() {
        String texto = SintomaAnalisis.textoRegistro("mareo", 2, LocalDateTime.of(2026, 1, 2, 8, 5), null);

        assertEquals("mareo, intensidad 2 de 10, leve. El 2 de enero de 2026 a las 08:05.", texto);
    }

    @Test
    void inicioYFinDeSemana_VanDeLunesADomingo() {
        LocalDate miercoles = LocalDate.of(2026, 10, 14);

        LocalDate inicio = SintomaAnalisis.inicioPeriodo(miercoles, AgrupacionTiempo.SEMANA);

        assertEquals(LocalDate.of(2026, 10, 12), inicio);
        assertEquals(LocalDate.of(2026, 10, 18), SintomaAnalisis.finPeriodo(inicio, AgrupacionTiempo.SEMANA));
        assertEquals(inicio, SintomaAnalisis.inicioPeriodo(LocalDate.of(2026, 10, 18), AgrupacionTiempo.SEMANA));
    }

    @Test
    void inicioYFinDeMes_RespetanMesesDeDistintaDuracion() {
        assertEquals(LocalDate.of(2026, 10, 1), SintomaAnalisis.inicioPeriodo(LocalDate.of(2026, 10, 14), AgrupacionTiempo.MES));
        assertEquals(LocalDate.of(2026, 10, 31), SintomaAnalisis.finPeriodo(LocalDate.of(2026, 10, 1), AgrupacionTiempo.MES));
        assertEquals(LocalDate.of(2028, 2, 29), SintomaAnalisis.finPeriodo(LocalDate.of(2028, 2, 1), AgrupacionTiempo.MES));
    }

    @Test
    void evolucion_AgrupaPorDiaConPromedioYMaximo() {
        SintomaEvolucionResponse evolucion = SintomaAnalisis.evolucion(
                "dolor de cabeza", DESDE, HASTA, AgrupacionTiempo.DIA, dolorDeCabezaQueEmpeora());

        assertEquals(4, evolucion.puntos().size());
        SintomaEvolucionResponse.Punto primero = evolucion.puntos().getFirst();
        assertEquals(DESDE, primero.inicio());
        assertEquals(2, primero.cantidad());
        assertEquals(4.0, primero.intensidadPromedio());
        assertEquals(5, primero.intensidadMaxima());
    }

    @Test
    void evolucion_DetectaQueElSintomaVaEmpeorando() {
        SintomaEvolucionResponse evolucion = SintomaAnalisis.evolucion(
                "dolor de cabeza", DESDE, HASTA, AgrupacionTiempo.DIA, dolorDeCabezaQueEmpeora());

        assertEquals(Tendencia.EMPEORANDO, evolucion.tendencia());
        assertTrue(evolucion.textoLectura().contains("empeorando"));
    }

    @Test
    void evolucion_DetectaQueElSintomaVaMejorando() {
        List<Registro> registros = List.of(
                registro("2026-10-01T08:00:00", "dolor", 9),
                registro("2026-10-02T08:00:00", "dolor", 8),
                registro("2026-10-03T08:00:00", "dolor", 4),
                registro("2026-10-04T08:00:00", "dolor", 3));

        assertEquals(Tendencia.MEJORANDO,
                SintomaAnalisis.evolucion("dolor", DESDE, HASTA, AgrupacionTiempo.DIA, registros).tendencia());
    }

    @Test
    void evolucion_ConIntensidadConstanteEsEstable() {
        List<Registro> registros = List.of(
                registro("2026-10-01T08:00:00", "dolor", 5),
                registro("2026-10-02T08:00:00", "dolor", 5),
                registro("2026-10-03T08:00:00", "dolor", 5));

        assertEquals(Tendencia.ESTABLE,
                SintomaAnalisis.evolucion("dolor", DESDE, HASTA, AgrupacionTiempo.DIA, registros).tendencia());
    }

    @Test
    void evolucion_ConUnSoloPeriodoNoTieneBaseParaComparar() {
        SintomaEvolucionResponse evolucion = SintomaAnalisis.evolucion(
                "dolor de cabeza", DESDE, HASTA, AgrupacionTiempo.MES, dolorDeCabezaQueEmpeora());

        assertEquals(1, evolucion.puntos().size());
        assertEquals(Tendencia.SIN_DATOS, evolucion.tendencia());
    }

    @Test
    void evolucion_AgrupaPorSemanaEmpezandoEnLunes() {
        SintomaEvolucionResponse evolucion = SintomaAnalisis.evolucion(
                null, DESDE, HASTA, AgrupacionTiempo.SEMANA, dolorDeCabezaQueEmpeora());

        assertEquals(3, evolucion.puntos().size());
        assertEquals(LocalDate.of(2026, 9, 28), evolucion.puntos().getFirst().inicio());
        assertNull(evolucion.tipo());
    }

    @Test
    void evolucion_SinRegistrosDevuelveListaVaciaYMensaje() {
        SintomaEvolucionResponse evolucion = SintomaAnalisis.evolucion(
                "dolor", DESDE, HASTA, AgrupacionTiempo.DIA, List.of());

        assertTrue(evolucion.puntos().isEmpty());
        assertEquals(Tendencia.SIN_DATOS, evolucion.tendencia());
        assertTrue(evolucion.textoLectura().contains("no hay registros"));
    }

    @Test
    void resumen_CalculaTotalesYPromedios() {
        SintomaResumenResponse resumen = SintomaAnalisis.resumen(DESDE, HASTA, dolorDeCabezaQueEmpeora());

        assertEquals(5, resumen.totalRegistros());
        assertEquals(4, resumen.diasConRegistro());
        assertEquals(5.8, resumen.intensidadPromedio());
        assertEquals(9, resumen.intensidadMaxima());
        assertEquals(3, resumen.intensidadMinima());
        assertEquals(1, resumen.porTipo().size());
        assertEquals(5, resumen.porTipo().getFirst().cantidad());
        assertEquals(9, resumen.porTipo().getFirst().ultimaIntensidad());
    }

    @Test
    void resumen_SinRegistrosDejaLosPromediosEnNull() {
        SintomaResumenResponse resumen = SintomaAnalisis.resumen(DESDE, HASTA, List.of());

        assertEquals(0, resumen.totalRegistros());
        assertNull(resumen.intensidadPromedio());
        assertTrue(resumen.porTipo().isEmpty());
        assertTrue(resumen.textoLectura().contains("no hay síntomas registrados"));
    }

    @Test
    void resumen_AgrupaTiposSinDistinguirMayusculasYOrdenaPorFrecuencia() {
        SintomaResumenResponse resumen = SintomaAnalisis.resumen(DESDE, HASTA, List.of(
                registro("2026-10-01T08:00:00", "Mareo", 4),
                registro("2026-10-02T08:00:00", "fiebre", 6),
                registro("2026-10-03T08:00:00", "mareo", 2)));

        assertEquals(2, resumen.porTipo().size());
        assertEquals("mareo", resumen.porTipo().getFirst().tipo()); // se muestra la escritura más reciente
        assertEquals(2, resumen.porTipo().getFirst().cantidad());
    }
}
