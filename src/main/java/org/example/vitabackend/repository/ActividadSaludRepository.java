package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.example.vitabackend.domain.model.ActividadSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActividadSaludRepository extends JpaRepository<ActividadSalud, Long> {
    List<ActividadSalud> findByUsuarioIdOrderByFechaHoraAsc(Long usuarioId);
    List<ActividadSalud> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime desde, LocalDateTime hasta);
}

