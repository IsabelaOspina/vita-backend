package org.example.vitabackend.repository;

import java.util.Optional;
import org.example.vitabackend.domain.model.Recomendacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    Page<Recomendacion> findByPaciente_Id(Long pacienteId, Pageable pageable);

    Page<Recomendacion> findByPaciente_IdAndLeidaFalse(Long pacienteId, Pageable pageable);

    long countByPaciente_IdAndLeidaFalse(Long pacienteId);

    Optional<Recomendacion> findByIdAndPaciente_Id(Long id, Long pacienteId);

    /** Lo que un profesional le ha escrito a un paciente. */
    Page<Recomendacion> findByPaciente_IdAndProfesional_Id(Long pacienteId, Long profesionalId, Pageable pageable);
}
