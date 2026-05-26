package com.sanly.appointments.repository;

import com.sanly.appointments.entity.ServiceStatus;
import com.sanly.appointments.entity.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceTypeRepository extends JpaRepository<ServiceType, UUID> {

    List<ServiceType> findByOfficeCodeAndStatus(String officeCode, ServiceStatus status);

    List<ServiceType> findByOfficeCode(String officeCode);
}
