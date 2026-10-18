package org.example.vitabackend.repository;

import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.model.HorarioMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HorarioMedicamentoRepository extends JpaRepository<HorarioMedicamento, Long> {
    List<HorarioMedicamento> findByMedicamento_IdAndMedicamento_Usuario_IdOrderByIdAsc(
            Long medicamentoId, Long usuarioId);
    Optional<HorarioMedicamento> findByIdAndMedicamento_IdAndMedicamento_Usuario_Id(
            Long id, Long medicamentoId, Long usuarioId);
    List<HorarioMedicamento> findByActivoTrue();
    List<HorarioMedicamento> findByActivoTrueAndMedicamento_Usuario_Id(Long usuarioId);
}
