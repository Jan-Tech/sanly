package com.sanly.land.repository;

import com.sanly.land.entity.Property;
import com.sanly.land.entity.PropertyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
    Optional<Property> findByCadastralNumber(String cadastralNumber);
    boolean existsByCadastralNumber(String cadastralNumber);
    List<Property> findByRegion(String region);
    List<Property> findByStatus(PropertyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Property p WHERE p.cadastralNumber = :cadastralNumber")
    Optional<Property> findByCadastralNumberWithLock(String cadastralNumber);
}
