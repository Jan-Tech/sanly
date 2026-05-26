package com.sanly.pension.repository;

import com.sanly.pension.entity.AccountSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface AccountSequenceRepository extends JpaRepository<AccountSequence, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AccountSequence s WHERE s.id = 1")
    Optional<AccountSequence> findForUpdate();
}
