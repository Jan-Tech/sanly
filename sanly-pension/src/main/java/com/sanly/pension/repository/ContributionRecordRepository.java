package com.sanly.pension.repository;

import com.sanly.pension.entity.ContributionRecord;
import com.sanly.pension.entity.ContributionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ContributionRecordRepository extends JpaRepository<ContributionRecord, Long> {
    List<ContributionRecord> findByAccountCodeOrderByContributionMonthDesc(String accountCode);
    List<ContributionRecord> findByEmployerCodeOrderByContributionMonthDesc(String employerCode);
    long countByAccountCodeAndStatus(String accountCode, ContributionStatus status);
    boolean existsByAccountCodeAndEmployerCodeAndContributionMonthAndStatusNot(
            String accountCode, String employerCode, String month, ContributionStatus status);

    @Query("SELECT DISTINCT c.employerCode FROM ContributionRecord c WHERE c.contributionMonth = :month AND c.status != 'REJECTED'")
    List<String> findEmployerCodesWithContributionForMonth(@Param("month") String month);
}
