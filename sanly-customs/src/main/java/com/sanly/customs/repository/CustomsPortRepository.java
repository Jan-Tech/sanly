package com.sanly.customs.repository;

import com.sanly.customs.entity.CustomsPort;
import com.sanly.customs.entity.PortStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomsPortRepository extends JpaRepository<CustomsPort, UUID> {
    Optional<CustomsPort> findByPortCode(String portCode);
    boolean existsByPortCode(String portCode);
    List<CustomsPort> findByStatus(PortStatus status);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(p.portCode, 9) AS int)), 0) FROM CustomsPort p WHERE p.portCode LIKE 'TM-PORT-%'")
    int findMaxPortSequence();
}
