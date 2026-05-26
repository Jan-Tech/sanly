package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.PlateSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface PlateSequenceRepository extends JpaRepository<PlateSequence, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PlateSequence p WHERE p.id = 1")
    Optional<PlateSequence> findForUpdate();
}
