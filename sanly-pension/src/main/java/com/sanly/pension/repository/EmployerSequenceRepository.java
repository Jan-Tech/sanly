package com.sanly.pension.repository;

import com.sanly.pension.entity.EmployerSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface EmployerSequenceRepository extends JpaRepository<EmployerSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM EmployerSequence s WHERE s.id = 1")
    Optional<EmployerSequence> findForUpdate();
}
