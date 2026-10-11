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
@Table(name = "solicitud_emergencia")
public class SolicitudEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private TipoEmergencia tipo;

    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EstadoEmergencia estado = EstadoEmergencia.PENDIENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_emergencia_id")
    private ContactoEmergencia contactoEmergencia;

}
