package org.example.vitabackend.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

/**
 * Código de un solo uso para restablecer la contraseña. El código NO se guarda en claro:
 * solo su hash (como la contraseña), así que nadie que lea la base de datos puede usarlo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recuperacion_contrasena")
public class RecuperacionContrasena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String codigoHash;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    /** Intentos fallidos con este código; al llegar al máximo se invalida. */
    @Column(nullable = false)
    @Builder.Default
    private Integer intentos = 0;

    /** true cuando ya se usó o se invalidó. */
    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;
}
