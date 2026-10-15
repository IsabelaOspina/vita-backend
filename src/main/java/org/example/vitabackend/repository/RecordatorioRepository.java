package org.example.vitabackend.repository;

import org.example.vitabackend.domain.enums.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.vitabackend.domain.model.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {
    List<Recordatorio> findByEstadoAndFechaHoraBefore(EstadoRecordatorio estado, LocalDateTime fecha);

    /** Busca recordatorios pendientes futuros que deben cancelarse junto con una actividad. */
    List<Recordatorio> findByActividadSalud_IdAndEstadoAndFechaHoraAfter(
            Long actividadSaludId, EstadoRecordatorio estado, LocalDateTime fecha);

    List<Recordatorio> findByActividadSalud_IdAndActividadSalud_Usuario_IdOrderByFechaHoraAscIdAsc(
            Long actividadSaludId, Long usuarioId);

    @Query("""
        select r from Recordatorio r
        left join r.actividadSalud a
        left join a.usuario au
        left join r.tomaMedicamento t
        left join t.medicamento m
        left join m.usuario mu
        where r.fechaHora >= :desde and r.fechaHora < :hasta
          and (au.id = :usuarioId or mu.id = :usuarioId)
        order by r.fechaHora asc, r.id asc
        """)
    List<Recordatorio> findAgendaByUsuarioIdAndIntervalo(
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hastaExclusivo);

    @Query("""
        select r from Recordatorio r
        left join r.actividadSalud a
        left join a.usuario au
        left join r.tomaMedicamento t
        left join t.medicamento m
        left join m.usuario mu
        where r.id = :recordatorioId
          and (au.id = :usuarioId or mu.id = :usuarioId)
        """)
    Optional<Recordatorio> findPropioById(
            @Param("recordatorioId") Long recordatorioId,
            @Param("usuarioId") Long usuarioId);

    List<Recordatorio> findByTomaMedicamento_Id(Long tomaMedicamentoId);
}
