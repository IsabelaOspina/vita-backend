package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Una recomendación breve que un profesional de la salud le deja a uno de sus pacientes,
 * normalmente a partir de los síntomas que el paciente registró.
 * Del síntoma se guarda su id y un texto de referencia: así la
 * recomendación se sigue entendiendo aunque el paciente luego edite o elimine ese síntoma.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recomendacion", indexes = {
        @Index(name = "idx_recomendacion_paciente_fecha", columnList = "paciente_id, fecha_creacion")
})
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    /** Cuenta (usuario) del profesional que escribió la recomendación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    private Usuario profesional;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    /** Síntoma al que se refiere (opcional). */
    private Long sintomaId;

    /** Ej.: "dolor de cabeza, intensidad 7 de 10, 14 de octubre de 2026". */
    @Column(length = 300)
    private String sintomaReferencia;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean leida = false;

    /** Cuándo la abrió el paciente (null mientras no la lea). */
    private LocalDateTime fechaLectura;
}
