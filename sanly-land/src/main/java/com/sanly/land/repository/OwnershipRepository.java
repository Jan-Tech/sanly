package com.sanly.land.repository;

import com.sanly.land.entity.Ownership;
import com.sanly.land.entity.OwnershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OwnershipRepository extends JpaRepository<Ownership, UUID> {
    List<Ownership> findByCadastralNumber(String cadastralNumber);
    List<Ownership> findByOwnerNationalId(String ownerNationalId);
    List<Ownership> findByCadastralNumberAndStatus(String cadastralNumber, OwnershipStatus status);
    List<Ownership> findByOwnerNationalIdAndStatus(String ownerNationalId, OwnershipStatus status);

    @Modifying
    @Query("UPDATE Ownership o SET o.status = :status WHERE o.ownerNationalId = :nationalId AND o.status = 'ACTIVE'")
    int updateStatusForOwner(String nationalId, OwnershipStatus status);
}
