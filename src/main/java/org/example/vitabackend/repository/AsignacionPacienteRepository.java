package org.example.vitabackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.model.AsignacionPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * "PersonalSalud_Usuario_Id" = id de la cuenta (Usuario) del profesional,
 * que es el id que maneja el frontend.
 */
@Repository
public interface AsignacionPacienteRepository extends JpaRepository<AsignacionPaciente, Long> {

    boolean existsByPersonalSalud_Usuario_IdAndPaciente_Id(Long profesionalId, Long pacienteId);

    boolean existsByPaciente_IdAndPersonalSalud_EspecialidadIgnoreCase(Long pacienteId, String especialidad);

    Optional<AsignacionPaciente> findByPersonalSalud_Usuario_IdAndPaciente_Id(Long profesionalId, Long pacienteId);

    List<AsignacionPaciente> findByPersonalSalud_Usuario_Id(Long profesionalId);

    List<AsignacionPaciente> findByPaciente_Id(Long pacienteId);
}
