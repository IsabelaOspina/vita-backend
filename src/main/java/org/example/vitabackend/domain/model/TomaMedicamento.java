package org.example.vitabackend.domain.model;

import org.example.vitabackend.domain.enums.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "toma_medicamento", uniqueConstraints = {
        @UniqueConstraint(name = "uk_toma_horario_fecha", columnNames = {"horario_medicamento_id", "fecha_hora"})
})
public class TomaMedicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoTomaMed estado = EstadoTomaMed.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id", nullable = false)
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horario_medicamento_id")
    private HorarioMedicamento horarioMedicamento;
}
