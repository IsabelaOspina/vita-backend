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
@Table(name = "recordatorio")
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String mensaje;

    @Enumerated(EnumType.STRING)
    private TipoRecordatorio tipoRecordatorio;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoRecordatorio estado = EstadoRecordatorio.PENDIENTE;

    // Un recordatorio pertenece a una actividad de salud O a una toma de medicamento
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_salud_id")
    private ActividadSalud actividadSalud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "toma_medicamento_id")
    private TomaMedicamento tomaMedicamento;

}
