package org.example.vitabackend.domain.enums;

/** Tipos de recurrencia soportados para generar tomas programadas. */
public enum TipoFrecuenciaMedicamento {
    /** Repite cada N horas desde la fecha y hora de inicio. */
    CADA_X_HORAS,
    /** Repite todos los días a una hora local. */
    DIARIO,
    /** Repite a una hora local en los días de la semana seleccionados. */
    DIAS_SEMANA
}
