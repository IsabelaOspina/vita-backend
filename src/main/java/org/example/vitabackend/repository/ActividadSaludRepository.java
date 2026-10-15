package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Acceso a datos de actividades; las consultas utilizadas por el servicio respetan el propietario. */
@Repository
public interface ActividadSaludRepository extends JpaRepository<ActividadSalud, Long> {
    List<ActividadSalud> findByUsuarioIdOrderByFechaHoraAsc(Long usuarioId);
    List<ActividadSalud> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime desde, LocalDateTime hasta);

    /** Lista actividades de un usuario en orden cronológico */
    List<ActividadSalud> findByUsuario_IdOrderByFechaHoraAscIdAsc(Long usuarioId);

    /** Consulta de actividades dentro de un rango, en orden cronológico. */
    List<ActividadSalud> findByUsuario_IdAndFechaHoraBetweenOrderByFechaHoraAscIdAsc(
            Long usuarioId, LocalDateTime desde, LocalDateTime hasta);

    /** Impide recuperar o modificar por error actividades pertenecientes a otro usuario. */
    Optional<ActividadSalud> findByIdAndUsuario_Id(Long id, Long usuarioId);

}

