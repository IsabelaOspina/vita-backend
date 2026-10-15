package org.example.vitabackend.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.example.vitabackend.domain.enums.EstadoTomaMed;
import org.example.vitabackend.domain.model.TomaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TomaMedicamentoRepository extends JpaRepository<TomaMedicamento, Long> {
    List<TomaMedicamento> findByMedicamentoId(Long medicamentoId);
    Optional<TomaMedicamento> findByIdAndMedicamento_Usuario_Id(Long id, Long usuarioId);
    List<TomaMedicamento> findByHorarioMedicamento_IdAndMedicamento_IdAndMedicamento_Usuario_IdAndFechaHoraGreaterThanEqualAndFechaHoraLessThanOrderByFechaHoraAscIdAsc(
            Long horarioId, Long medicamentoId, Long usuarioId, LocalDateTime desde, LocalDateTime hastaExclusivo);
    boolean existsByHorarioMedicamento_IdAndFechaHora(Long horarioId, LocalDateTime fechaHora);
    List<TomaMedicamento> findByHorarioMedicamento_IdAndFechaHoraAfterAndEstadoIn(
            Long horarioId, LocalDateTime desde, Collection<EstadoTomaMed> estados);
}

