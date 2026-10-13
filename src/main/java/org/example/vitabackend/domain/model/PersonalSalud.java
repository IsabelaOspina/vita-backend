package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Datos del profesional de la salud (uno por cuenta con rol PERSONAL_SALUD). Se guardan aparte para que la tabla usuario
 * no cargue columnas vacías para los pacientes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "personal_salud")
public class PersonalSalud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(name = "numero_registro_profesional", nullable = false, unique = true, length = 50)
    private String numeroRegistroProfesional;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

}
