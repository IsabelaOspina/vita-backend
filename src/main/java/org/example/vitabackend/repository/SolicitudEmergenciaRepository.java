package org.example.vitabackend.repository;

import java.util.List;

import org.example.vitabackend.domain.model.SolicitudEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolicitudEmergenciaRepository extends JpaRepository<SolicitudEmergencia, Long> {
    List<SolicitudEmergencia> findByUsuarioIdOrderByFechaHoraDesc(Long usuarioId);
}

