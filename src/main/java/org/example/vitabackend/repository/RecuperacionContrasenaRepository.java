package org.example.vitabackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.model.RecuperacionContrasena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecuperacionContrasenaRepository extends JpaRepository<RecuperacionContrasena, Long> {

    /** El código más reciente que todavía no se ha usado. */
    Optional<RecuperacionContrasena> findFirstByUsuario_IdAndUsadoFalseOrderByIdDesc(Long usuarioId);

    List<RecuperacionContrasena> findByUsuario_IdAndUsadoFalse(Long usuarioId);

    /** El último código pedido (usado o no), para limitar cuántos se piden seguidos. */
    Optional<RecuperacionContrasena> findFirstByUsuario_IdOrderByIdDesc(Long usuarioId);
}
