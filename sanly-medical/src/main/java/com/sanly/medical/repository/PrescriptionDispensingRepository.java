package com.sanly.medical.repository;

import com.sanly.medical.entity.PrescriptionDispensing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PrescriptionDispensingRepository extends JpaRepository<PrescriptionDispensing, UUID> {
    List<PrescriptionDispensing> findByPrescriptionCodeOrderByDispensedAtDesc(String prescriptionCode);
}
