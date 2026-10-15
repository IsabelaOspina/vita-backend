package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import org.example.vitabackend.domain.enums.TipoFrecuenciaMedicamento;
import org.example.vitabackend.domain.enums.TipoRecordatorio;

/** Regla recurrente para producir ocurrencias concretas de toma de un medicamento. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "horario_medicamento")
public class HorarioMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_frecuencia", nullable = false, length = 30)
    private TipoFrecuenciaMedicamento tipoFrecuencia;

    /** Inicio efectivo, y ancla temporal para CADA_X_HORAS. */
    @Column(name = "fecha_hora_inicio", nullable = false)
    private LocalDateTime fechaHoraInicio;

    /** Último instante permitido; NULL significa que la regla continúa indefinidamente. */
    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;

    /** Obligatorio únicamente cuando tipoFrecuencia=CADA_X_HORAS. */
    @Column(name = "intervalo_horas")
    private Integer intervaloHoras;

    /** Obligatorio para DIARIO y DIAS_SEMANA. */
    @Column(name = "hora_programada")
    private LocalTime horaProgramada;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "horario_medicamento_dia",
            joinColumns = @JoinColumn(name = "horario_medicamento_id"))
    @Column(name = "dia_semana", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> diasSemana = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recordatorio", nullable = false, length = 20)
    @Builder.Default
    private TipoRecordatorio tipoRecordatorio = TipoRecordatorio.VISUAL;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean activo = true;
}
