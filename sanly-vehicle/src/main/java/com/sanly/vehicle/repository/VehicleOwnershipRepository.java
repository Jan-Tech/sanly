package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.OwnershipStatus;
import com.sanly.vehicle.entity.VehicleOwnership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleOwnershipRepository extends JpaRepository<VehicleOwnership, Long> {
    List<VehicleOwnership> findByPlateNumberOrderByOwnershipStartDateDesc(String plateNumber);
    Optional<VehicleOwnership> findByPlateNumberAndStatus(String plateNumber, OwnershipStatus status);
    List<VehicleOwnership> findByOwnerNationalIdAndStatus(String ownerNationalId, OwnershipStatus status);
    List<VehicleOwnership> findByOwnerNationalId(String ownerNationalId);
}
