package com.sanly.appointments.repository;

import com.sanly.appointments.entity.WaitlistEntry;
import com.sanly.appointments.entity.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, UUID> {

    List<WaitlistEntry> findByOfficeCodeAndStatusOrderByCreatedAtAsc(String officeCode, WaitlistStatus status);

    List<WaitlistEntry> findByStatusAndCreatedAtBefore(WaitlistStatus status, LocalDateTime before);

    List<WaitlistEntry> findByCitizenNationalId(String citizenNationalId);

    List<WaitlistEntry> findByOfficeCodeAndServiceTypeIdAndStatusOrderByCreatedAtAsc(
            String officeCode, UUID serviceTypeId, WaitlistStatus status);

    List<WaitlistEntry> findByStatusAndNotifiedAtBefore(WaitlistStatus status, LocalDateTime before);
}
