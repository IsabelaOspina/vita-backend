package org.example.vitabackend.repository;

import java.util.List;

import org.example.vitabackend.domain.model.TomaMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TomaMedicamentoRepository extends JpaRepository<TomaMedicamento, Long> {
    List<TomaMedicamento> findByMedicamentoId(Long medicamentoId);
}

