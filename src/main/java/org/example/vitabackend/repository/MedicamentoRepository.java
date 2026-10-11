package org.example.vitabackend.repository;

import java.util.List;

import org.example.vitabackend.domain.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {
    List<Medicamento> findByUsuarioId(Long usuarioId);
}
