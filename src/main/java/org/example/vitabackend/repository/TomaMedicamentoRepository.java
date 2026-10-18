package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TomaMedicamentoRepository extends JpaRepository<TomaMedicamento, Long> {
    List<TomaMedicamento> findByMedicamentoId(Long medicamentoId);
    Optional<TomaMedicamento> findByIdAndMedicamento_Usuario_Id(Long id, Long usuarioId);
    List<TomaMedicamento> findByHorarioMedicamento_IdAndMedicamento_IdAndMedicamento_Usuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
            Long horarioId, Long medicamentoId, Long usuarioId, LocalDateTime desde, LocalDateTime hastaExclusivo);
    boolean existsByHorarioMedicamento_IdAndFechaHora(Long horarioId, LocalDateTime fechaHora);
    List<TomaMedicamento> findByHorarioMedicamento_IdAndFechaHoraAfterAndEstadoIn(
            Long horarioId, LocalDateTime desde, Collection<EstadoTomaMed> estados);

    /**
     * Todas las tomas de un usuario en un rango, de sus medicamentos y horarios activos, en una sola
     * consulta (con el medicamento cargado para no consultar uno por uno).
     */
    @Query("""
        select t from TomaMedicamento t
        join fetch t.medicamento m
        join t.horarioMedicamento h
        where m.usuario.id = :usuarioId
          and m.activo = true and h.activo = true
          and t.fechaHora >= :desde and t.fechaHora < :hasta
        order by t.fechaHora asc, t.id asc
        """)
    List<TomaMedicamento> findDelUsuarioEntre(@Param("usuarioId") Long usuarioId,
                                              @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    /** Fechas que ya tienen toma en un horario (para generar solo las que faltan, con una sola consulta). */
    @Query("""
        select t.fechaHora from TomaMedicamento t
        where t.horarioMedicamento.id = :horarioId
          and t.fechaHora >= :desde and t.fechaHora <= :hasta
        """)
    List<LocalDateTime> findFechasDeHorarioEntre(@Param("horarioId") Long horarioId,
                                                 @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}

