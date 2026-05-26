package com.sanly.documents.repository;

import com.sanly.documents.entity.TrackedItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrackedItemRepository extends JpaRepository<TrackedItem, UUID> {

    List<TrackedItem> findByCitizenNationalIdOrderByLastUpdatedAtDesc(String citizenNationalId);

    Optional<TrackedItem> findBySourceItemCode(String sourceItemCode);

    Optional<TrackedItem> findByTrackingCode(String trackingCode);

    List<TrackedItem> findByCitizenNationalIdAndIsCompleted(String citizenNationalId, boolean isCompleted);

    Page<TrackedItem> findAllByOrderByLastUpdatedAtDesc(Pageable pageable);
}
