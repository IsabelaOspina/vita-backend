package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.example.vitabackend.domain.enums.Rol;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cedula;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String correo;

    @Column(nullable = false)
    private String contrasena;

    private String telefono;

    private LocalDate fechaNacimiento;

    private String direccion;

    @Builder.Default
    private Boolean activo = true;

    // Se guarda como VARCHAR (no como ENUM de MySQL) para poder agregar roles sin alterar la columna.
    // El default evita que las filas que ya existían queden sin rol al actualizar el esquema.
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'USUARIO'")
    @Builder.Default
    private Rol rol = Rol.USUARIO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contacto_emergencia_id")
    private ContactoEmergencia contactoEmergencia;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Discapacidad> discapacidades = new ArrayList<>();

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ConfiguracionAccesibilidad configuracionAccesibilidad;

}