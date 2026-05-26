package com.sanly.land.repository;

import com.sanly.land.entity.TransferApplication;
import com.sanly.land.entity.TransferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferApplicationRepository extends JpaRepository<TransferApplication, UUID> {
    List<TransferApplication> findByCadastralNumber(String cadastralNumber);
    List<TransferApplication> findByFromNationalId(String fromNationalId);
    List<TransferApplication> findByToNationalId(String toNationalId);
    List<TransferApplication> findByStatus(TransferStatus status);

    @Query("SELECT t FROM TransferApplication t WHERE t.fromNationalId = :nationalId OR t.toNationalId = :nationalId")
    List<TransferApplication> findByCitizenNationalId(String nationalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TransferApplication t WHERE t.applicationId = :id")
    Optional<TransferApplication> findByIdWithLock(UUID id);
}
