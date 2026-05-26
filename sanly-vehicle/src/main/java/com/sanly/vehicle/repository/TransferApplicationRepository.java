package com.sanly.vehicle.repository;

import com.sanly.vehicle.entity.TransferApplication;
import com.sanly.vehicle.entity.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransferApplicationRepository extends JpaRepository<TransferApplication, Long> {
    List<TransferApplication> findByPlateNumberOrderByApplicationDateDesc(String plateNumber);
    List<TransferApplication> findByStatus(TransferStatus status);
    boolean existsByPlateNumberAndStatus(String plateNumber, TransferStatus status);
}
