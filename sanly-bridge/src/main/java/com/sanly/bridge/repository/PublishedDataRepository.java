package com.sanly.bridge.repository;

import com.sanly.bridge.entity.DataType;
import com.sanly.bridge.entity.PublishedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PublishedDataRepository extends JpaRepository<PublishedData, UUID> {

    /** Find all active, non-expired records for a citizen and data type. */
    @Query("""
        SELECT p FROM PublishedData p
        WHERE p.nationalId = :nationalId
          AND p.dataType   = :dataType
          AND p.active     = true
          AND (p.expiresAt IS NULL OR p.expiresAt > :now)
        ORDER BY p.publishedAt DESC
        """)
    List<PublishedData> findActive(
            @Param("nationalId") String nationalId,
            @Param("dataType")   DataType dataType,
            @Param("now")        LocalDateTime now
    );

    /** Same but filtered to a specific publisher. */
    @Query("""
        SELECT p FROM PublishedData p
        WHERE p.nationalId   = :nationalId
          AND p.dataType     = :dataType
          AND p.publisherCode = :publisherCode
          AND p.active       = true
          AND (p.expiresAt IS NULL OR p.expiresAt > :now)
        ORDER BY p.publishedAt DESC
        """)
    List<PublishedData> findActiveByPublisher(
            @Param("nationalId")    String nationalId,
            @Param("dataType")      DataType dataType,
            @Param("publisherCode") String publisherCode,
            @Param("now")           LocalDateTime now
    );
}
