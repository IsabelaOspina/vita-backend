package org.example.vitabackend.repository;

import java.util.List;

import org.example.vitabackend.domain.model.Discapacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscapacidadRepository extends JpaRepository<Discapacidad, Long> {
    List<Discapacidad> findByUsuarioId(Long usuarioId);
}

