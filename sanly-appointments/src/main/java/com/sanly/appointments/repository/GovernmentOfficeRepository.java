package com.sanly.appointments.repository;

import com.sanly.appointments.entity.GovernmentOffice;
import com.sanly.appointments.entity.InstitutionType;
import com.sanly.appointments.entity.OfficeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GovernmentOfficeRepository extends JpaRepository<GovernmentOffice, UUID> {

    Optional<GovernmentOffice> findByOfficeCode(String officeCode);

    List<GovernmentOffice> findByStatusOrderByNameAsc(OfficeStatus status);

    List<GovernmentOffice> findByInstitutionTypeAndStatusOrderByNameAsc(InstitutionType institutionType, OfficeStatus status);

    List<GovernmentOffice> findByRegionAndStatusOrderByNameAsc(String region, OfficeStatus status);

    List<GovernmentOffice> findByInstitutionTypeAndRegionAndStatusOrderByNameAsc(
            InstitutionType institutionType, String region, OfficeStatus status);
}
