package org.example.vitabackend.repository;

import java.util.List;

import org.example.vitabackend.domain.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findBySolicitudEmergenciaId(Long solicitudId);
}
