package com.sanly.bridge.repository;

import com.sanly.bridge.entity.Institution;
import com.sanly.bridge.entity.InstitutionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, String> {

    Optional<Institution> findByInstitutionCode(String institutionCode);

    boolean existsByInstitutionCode(String institutionCode);

    List<Institution> findAllByStatus(InstitutionStatus status);
}
