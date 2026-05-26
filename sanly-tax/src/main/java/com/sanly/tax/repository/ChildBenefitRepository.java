package com.sanly.tax.repository;

import com.sanly.tax.entity.BenefitStatus;
import com.sanly.tax.entity.ChildBenefitRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ChildBenefitRepository extends JpaRepository<ChildBenefitRecord, UUID> {

    boolean existsByChildNationalId(String childNationalId);

    @Modifying
    @Query("UPDATE ChildBenefitRecord c SET c.status = :status, c.cancelledAt = :now " +
           "WHERE (c.childNationalId = :nin OR c.motherNationalId = :nin OR c.fatherNationalId = :nin) " +
           "AND c.status = 'ACTIVE'")
    int cancelAllForCitizen(@Param("nin") String nationalId,
                            @Param("status") BenefitStatus status,
                            @Param("now") LocalDateTime now);
}
