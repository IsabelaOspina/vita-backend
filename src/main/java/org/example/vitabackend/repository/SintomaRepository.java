package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.example.vitabackend.domain.model.Sintoma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SintomaRepository extends JpaRepository<Sintoma, Long> {
    List<Sintoma> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
    List<Sintoma> findByUsuarioIdAndFechaHoraBetween(Long usuarioId, LocalDateTime desde, LocalDateTime hasta);
}
