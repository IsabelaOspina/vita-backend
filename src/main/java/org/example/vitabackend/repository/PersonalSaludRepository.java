package org.example.vitabackend.repository;

import org.example.vitabackend.domain.model.PersonalSalud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalSaludRepository extends JpaRepository<PersonalSalud, Long> {
    boolean existsByNumeroRegistroProfesional(String numeroRegistroProfesional);
}
