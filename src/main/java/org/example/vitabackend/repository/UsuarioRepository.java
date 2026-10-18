package org.example.vitabackend.repository;

import java.util.List;
import java.util.Optional;

import org.example.vitabackend.domain.enums.Rol;
import org.example.vitabackend.domain.model.Usuario;
import org.example.vitabackend.security.Credenciales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    /**
     * Solo correo, contraseña, rol y estado, en una consulta. La usa la seguridad en cada petición:
     * cargar la cuenta completa traía además su configuración de accesibilidad y sus datos de
     * personal de salud (consultas extra que hacían más lenta cada pantalla).
     */
    @Query("select new org.example.vitabackend.security.Credenciales(u.correo, u.contrasena, u.rol, u.activo) "
            + "from Usuario u where u.correo = :correo")
    Optional<Credenciales> findCredencialesByCorreo(@Param("correo") String correo);
    boolean existsByCorreo(String correo);
    boolean existsByCedula(String cedula);
    boolean existsByRol(Rol rol);
    List<Usuario> findByRol(Rol rol);
}