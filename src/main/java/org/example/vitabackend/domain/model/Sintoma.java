package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Un síntoma registrado por un paciente (proceso 2: seguimiento de síntomas).
 * Un registro = un síntoma con su intensidad en un momento; la evolución se obtiene
 * comparando los registros del mismo tipo a lo largo del tiempo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sintoma", indexes = {
        @Index(name = "idx_sintoma_usuario_fecha", columnList = "usuario_id, fecha_hora")
})
public class Sintoma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cuándo ocurrió/sintió el síntoma (puede ser anterior a cuando se registró). */
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /** Tipo de síntoma en texto libre */
    @Column(nullable = false, length = 100)
    private String tipo;

    /** Escala 1 (muy leve) a 10 (insoportable). */
    @Column(nullable = false)
    private Integer intensidad;

    @Column(length = 1000)
    private String observaciones;

    /** Cuándo se guardó en el sistema (no se modifica). */
    @Column(nullable = false)
    private LocalDateTime fechaRegistro;

    /** Última vez que el paciente editó el registro. */
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
