package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "configuracion_accesibilidad")
public class ConfiguracionAccesibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default private Boolean altoContraste = false;
    @Builder.Default private Integer tamanoTexto = 16;
    @Builder.Default private Boolean lectorPantalla = false;
    @Builder.Default private Boolean navegacionSimple = false;
    @Builder.Default private Boolean notificacionesVoz = false;
    @Builder.Default private Boolean notificacionesSonoras = true;
    @Builder.Default private Boolean notificacionesVisuales = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

}
