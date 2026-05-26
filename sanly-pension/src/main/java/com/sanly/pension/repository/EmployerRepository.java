package com.sanly.pension.repository;

import com.sanly.pension.entity.Employer;
import com.sanly.pension.entity.EmployerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EmployerRepository extends JpaRepository<Employer, Long> {
    Optional<Employer> findByEmployerCode(String employerCode);
    Optional<Employer> findByBusinessRegistrationNumber(String businessRegistrationNumber);
    List<Employer> findByStatus(EmployerStatus status);
}
