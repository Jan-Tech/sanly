package com.sanly.analytics.repository;

import com.sanly.analytics.entity.ServiceUsageStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceUsageStatRepository extends JpaRepository<ServiceUsageStat, UUID> {
    List<ServiceUsageStat> findByStatDateBetweenOrderByStatDateDesc(LocalDate from, LocalDate to);
    List<ServiceUsageStat> findByServiceNameAndStatDateBetween(String serviceName, LocalDate from, LocalDate to);
    Optional<ServiceUsageStat> findByStatDateAndServiceNameAndEndpoint(LocalDate date, String serviceName, String endpoint);

    @Query("SELECT s FROM ServiceUsageStat s WHERE s.statDate = (SELECT MAX(s2.statDate) FROM ServiceUsageStat s2) ORDER BY s.requestCount DESC")
    List<ServiceUsageStat> findLatestTopServices();
}
