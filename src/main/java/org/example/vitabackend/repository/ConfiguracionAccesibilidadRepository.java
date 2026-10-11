package org.example.vitabackend.repository;

import java.util.Optional;
import org.example.vitabackend.domain.model.ConfiguracionAccesibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionAccesibilidadRepository extends JpaRepository<ConfiguracionAccesibilidad, Long> {
    Optional<ConfiguracionAccesibilidad> findByUsuarioId(Long usuarioId);
}
