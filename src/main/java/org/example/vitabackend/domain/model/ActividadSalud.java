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
@Table(name = "actividad_salud")
public class ActividadSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoActividad tipo;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String ubicacion;

    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoActividad estado = EstadoActividad.PENDIENTE;

    private LocalDateTime fechaHoraCumplimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

}
