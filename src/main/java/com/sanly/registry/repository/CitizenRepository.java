package com.sanly.registry.repository;

import com.sanly.registry.entity.Citizen;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, String> {

    /**
     * Multi-field search with optional filters. All three parameters are optional;
     * passing null skips that criterion. Name search covers both first and last name.
     * Note: firstName and lastName are not encrypted, so LIKE is safe here.
     */
    @Query("""
        SELECT c FROM Citizen c
        WHERE (:name   IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
                               OR LOWER(c.lastName)  LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:dob    IS NULL OR c.dateOfBirth = :dob)
          AND (:region IS NULL OR LOWER(c.address.region) LIKE LOWER(CONCAT('%', :region, '%')))
        """)
    Page<Citizen> search(
            @Param("name")   String name,
            @Param("dob")    LocalDate dob,
            @Param("region") String region,
            Pageable pageable
    );

    boolean existsByNationalId(String nationalId);
}
