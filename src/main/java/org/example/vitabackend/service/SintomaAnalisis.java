package org.example.vitabackend.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.example.vitabackend.domain.enums.AgrupacionTiempo;
import org.example.vitabackend.domain.enums.NivelIntensidad;
import org.example.vitabackend.domain.enums.Tendencia;
import org.example.vitabackend.dto.response.SintomaEvolucionResponse;
import org.example.vitabackend.dto.response.SintomaResumenResponse;

/**
 * Cálculos (sin base de datos ni Spring) del seguimiento de síntomas:
 * niveles, agrupación por periodo, tendencia, indicadores y textos de lectura.
 */
public final class SintomaAnalisis {

    private static final Locale ES = Locale.forLanguageTag("es-CO");
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES);
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm", ES);

    /** Diferencia mínima (en puntos de intensidad). */
    static final double UMBRAL_TENDENCIA = 0.5;

    private SintomaAnalisis() {
    }

    /** Lo mínimo que se necesita de un síntoma para analizarlo. */
    public record Registro(LocalDateTime fechaHora, String tipo, int intensidad) {
    }

    // ---------- básicos ----------

    public static NivelIntensidad nivel(int intensidad) {
        if (intensidad <= 3) {
            return NivelIntensidad.LEVE;
        }
        return intensidad <= 6 ? NivelIntensidad.MODERADA : NivelIntensidad.SEVERA;
    }

    /** "dolor de cabeza". */
    public static String normalizarTipo(String tipo) {
        return tipo == null ? "" : tipo.trim().replaceAll("\\s+", " ");
    }

    public static String claveTipo(String tipo) {
        return normalizarTipo(tipo).toLowerCase(ES);
    }

    public static double redondear(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }

    private static String decimal(double valor) {
        return String.format(ES, "%.1f", valor);
    }

    public static String fechaLegible(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    // ---------- texto para leer en voz alta ----------

    public static String textoRegistro(String tipo, int intensidad, LocalDateTime fechaHora, String observaciones) {
        StringBuilder sb = new StringBuilder();
        sb.append(tipo).append(", intensidad ").append(intensidad).append(" de 10, ")
                .append(nivel(intensidad).name().toLowerCase(ES)).append(". ")
                .append("El ").append(fechaHora.toLocalDate().format(FECHA))
                .append(" a las ").append(fechaHora.format(HORA)).append(".");
        if (observaciones != null && !observaciones.isBlank()) {
            sb.append(" Observaciones: ").append(observaciones.trim());
        }
        return sb.toString();
    }

    // ---------- periodos ----------

    public static LocalDate inicioPeriodo(LocalDate dia, AgrupacionTiempo agrupacion) {
        return switch (agrupacion) {
            case DIA -> dia;
            case SEMANA -> dia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MES -> dia.withDayOfMonth(1);
        };
    }

    public static LocalDate finPeriodo(LocalDate inicio, AgrupacionTiempo agrupacion) {
        return switch (agrupacion) {
            case DIA -> inicio;
            case SEMANA -> inicio.plusDays(6);
            case MES -> inicio.with(TemporalAdjusters.lastDayOfMonth());
        };
    }

    // ---------- evolución ----------

    public static List<SintomaEvolucionResponse.Punto> puntos(List<Registro> registros, AgrupacionTiempo agrupacion) {
        Map<LocalDate, List<Registro>> porPeriodo = new TreeMap<>();
        for (Registro r : registros) {
            porPeriodo.computeIfAbsent(inicioPeriodo(r.fechaHora().toLocalDate(), agrupacion),
                    k -> new ArrayList<>()).add(r);
        }
        List<SintomaEvolucionResponse.Punto> puntos = new ArrayList<>();
        porPeriodo.forEach((inicio, lista) -> puntos.add(new SintomaEvolucionResponse.Punto(
                inicio,
                finPeriodo(inicio, agrupacion),
                lista.size(),
                redondear(lista.stream().mapToInt(Registro::intensidad).average().orElse(0)),
                lista.stream().mapToInt(Registro::intensidad).max().orElse(0))));
        return puntos;
    }

    /**
     * Compara el promedio ponderado (por cantidad de registros) de la primera mitad de los periodos
     * con el de la segunda mitad (si hay un número impar, el periodo del medio se ignora).
     * Diferencia mayor a 0,5 puntos: EMPEORANDO si sube, MEJORANDO si baja; si no, ESTABLE.
     * Con menos de 2 periodos no hay base para comparar: SIN_DATOS.
     */
    public static Tendencia tendencia(List<SintomaEvolucionResponse.Punto> puntos) {
        int n = puntos.size();
        if (n < 2) {
            return Tendencia.SIN_DATOS;
        }
        double primera = promedioPonderado(puntos.subList(0, n / 2));
        double segunda = promedioPonderado(puntos.subList((n + 1) / 2, n));
        double diferencia = segunda - primera;
        if (diferencia > UMBRAL_TENDENCIA) {
            return Tendencia.EMPEORANDO;
        }
        if (diferencia < -UMBRAL_TENDENCIA) {
            return Tendencia.MEJORANDO;
        }
        return Tendencia.ESTABLE;
    }

    private static double promedioPonderado(List<SintomaEvolucionResponse.Punto> puntos) {
        double suma = 0;
        long total = 0;
        for (SintomaEvolucionResponse.Punto p : puntos) {
            suma += p.intensidadPromedio() * p.cantidad();
            total += p.cantidad();
        }
        return total == 0 ? 0 : suma / total;
    }

    public static SintomaEvolucionResponse evolucion(String tipo, LocalDate desde, LocalDate hasta,
                                                     AgrupacionTiempo agrupacion, List<Registro> registros) {
        List<SintomaEvolucionResponse.Punto> puntos = puntos(registros, agrupacion);
        Tendencia tendencia = tendencia(puntos);
        String sujeto = (tipo == null || tipo.isBlank()) ? "sus síntomas" : "\"" + tipo + "\"";
        String periodo = "Entre el " + fechaLegible(desde) + " y el " + fechaLegible(hasta) + ", ";
        String texto;
        if (puntos.isEmpty()) {
            texto = periodo + "no hay registros de " + sujeto + ".";
        } else {
            long total = puntos.stream().mapToLong(SintomaEvolucionResponse.Punto::cantidad).sum();
            texto = periodo + "hay " + total + (total == 1 ? " registro" : " registros") + " de " + sujeto + ". "
                    + switch (tendencia) {
                case MEJORANDO -> "La intensidad viene bajando: va mejorando.";
                case EMPEORANDO -> "La intensidad viene subiendo: va empeorando.";
                case ESTABLE -> "La intensidad se mantiene estable.";
                case SIN_DATOS -> "Aún no hay suficientes periodos para saber si cambia.";
            };
        }
        return new SintomaEvolucionResponse(
                (tipo == null || tipo.isBlank()) ? null : tipo, desde, hasta, agrupacion, puntos, tendencia, texto);
    }

    // ---------- resumen / KPIs ----------

    public static SintomaResumenResponse resumen(LocalDate desde, LocalDate hasta, List<Registro> registros) {
        long total = registros.size();
        if (total == 0) {
            return new SintomaResumenResponse(desde, hasta, 0, 0, null, null, null, List.of(),
                    "Entre el " + fechaLegible(desde) + " y el " + fechaLegible(hasta)
                            + " no hay síntomas registrados.");
        }

        long dias = registros.stream().map(r -> r.fechaHora().toLocalDate()).distinct().count();
        double promedio = registros.stream().mapToInt(Registro::intensidad).average().orElse(0);
        int max = registros.stream().mapToInt(Registro::intensidad).max().orElse(0);
        int min = registros.stream().mapToInt(Registro::intensidad).min().orElse(0);
        // Por tipo (sin distinguir mayúsculas); se muestra la escritura más reciente
        Map<String, List<Registro>> porTipo = new LinkedHashMap<>();
        registros.stream().sorted(Comparator.comparing(Registro::fechaHora).reversed())
                .forEach(r -> porTipo.computeIfAbsent(claveTipo(r.tipo()), k -> new ArrayList<>()).add(r));
        List<SintomaResumenResponse.TipoResumen> tipos = new ArrayList<>();
        for (List<Registro> lista : porTipo.values()) {
            Registro ultimo = lista.get(0); // la lista viene de más reciente a más antiguo
            tipos.add(new SintomaResumenResponse.TipoResumen(
                    normalizarTipo(ultimo.tipo()),
                    lista.size(),
                    redondear(lista.stream().mapToInt(Registro::intensidad).average().orElse(0)),
                    lista.stream().mapToInt(Registro::intensidad).max().orElse(0),
                    ultimo.fechaHora(),
                    ultimo.intensidad()));
        }
        tipos.sort(Comparator.comparingLong(SintomaResumenResponse.TipoResumen::cantidad).reversed()
                .thenComparing(SintomaResumenResponse.TipoResumen::tipo));

        SintomaResumenResponse.TipoResumen principal = tipos.get(0);
        String texto = "Entre el " + fechaLegible(desde) + " y el " + fechaLegible(hasta)
                + " registró " + total + (total == 1 ? " síntoma" : " síntomas")
                + " en " + dias + (dias == 1 ? " día" : " días") + ". "
                + "Intensidad promedio " + decimal(promedio) + " de 10, con un máximo de " + max + ". "
                + "El más frecuente fue " + principal.tipo() + " (" + principal.cantidad()
                + (principal.cantidad() == 1 ? " vez)." : " veces).");

        return new SintomaResumenResponse(desde, hasta, total, dias, redondear(promedio), max, min,
                tipos, texto);
    }
}
