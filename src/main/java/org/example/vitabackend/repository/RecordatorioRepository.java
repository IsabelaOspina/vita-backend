package org.example.vitabackend.repository;

import org.example.vitabackend.domain.enums.*;
import java.time.LocalDateTime;
import java.util.List;

import org.example.vitabackend.domain.model.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordatorioRepository extends JpaRepository<Recordatorio, Long> {
    List<Recordatorio> findByEstadoAndFechaHoraBefore(EstadoRecordatorio estado, LocalDateTime fecha);
}
