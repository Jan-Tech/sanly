package com.sanly.banking.repository;

import com.sanly.banking.entity.ConsentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConsentRequestRepository extends JpaRepository<ConsentRequest, UUID> {

    Optional<ConsentRequest> findByConsentCode(String consentCode);

    List<ConsentRequest> findByCitizenNationalIdAndStatus(String citizenNationalId, String status);

    List<ConsentRequest> findByBankCodeAndStatus(String bankCode, String status);

    List<ConsentRequest> findByStatusAndRequestedAtBefore(String status, LocalDateTime cutoff);

    Page<ConsentRequest> findAllByOrderByRequestedAtDesc(Pageable pageable);
}
