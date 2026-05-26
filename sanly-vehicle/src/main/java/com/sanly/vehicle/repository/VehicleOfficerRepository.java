package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.VehicleOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleOfficerRepository extends JpaRepository<VehicleOfficer, Long> {
    Optional<VehicleOfficer> findByUsername(String username);
}
