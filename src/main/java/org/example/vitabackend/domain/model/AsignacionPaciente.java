package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Relación entre un profesional de la salud y un paciente (N a N).
 * Un paciente puede tener varios profesionales, pero de distinta especialidad
 * (esa regla la valida AsignacionServiceImpl).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "asignacion_paciente",
        uniqueConstraints = @UniqueConstraint(columnNames = {"personal_salud_id", "paciente_id"}))
public class AsignacionPaciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_salud_id", nullable = false)
    private PersonalSalud personalSalud;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    @Column(nullable = false)
    private LocalDateTime fechaAsignacion;

}
