package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.Vehicle;
import com.sanly.vehicle.entity.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
    Optional<Vehicle> findByVin(String vin);
    List<Vehicle> findByStatus(VehicleStatus status);
    boolean existsByPlateNumber(String plateNumber);
    boolean existsByVin(String vin);
}
