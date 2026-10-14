package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.model.Sintoma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Todas las consultas filtran por usuario_id.
 * Los filtros "vacíos" se reemplazan en el servicio por valores que no filtran
 * "hasta" es EXCLUSIVO (el servicio manda el día siguiente a medianoche).
 */
@Repository
public interface SintomaRepository extends JpaRepository<Sintoma, Long> {

    Optional<Sintoma> findByIdAndUsuario_Id(Long id, Long usuarioId);

    List<Sintoma> findByUsuario_IdOrderByFechaHoraDesc(Long usuarioId);

    @Query("""
            select s from Sintoma s
            where s.usuario.id = :usuarioId
              and s.fechaHora >= :desde and s.fechaHora < :hasta
              and lower(s.tipo) like :patron
              and s.intensidad between :minimo and :maximo
            """)
    Page<Sintoma> buscar(@Param("usuarioId") Long usuarioId,
                         @Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta,
                         @Param("patron") String patron,
                         @Param("minimo") int minimo,
                         @Param("maximo") int maximo,
                         Pageable pageable);

    @Query("""
            select s from Sintoma s
            where s.usuario.id = :usuarioId
              and s.fechaHora >= :desde and s.fechaHora < :hasta
              and lower(s.tipo) like :patron
            order by s.fechaHora asc, s.id asc
            """)
    List<Sintoma> buscarEnRango(@Param("usuarioId") Long usuarioId,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta,
                                @Param("patron") String patron);
}
