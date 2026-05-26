package com.sanly.dmv.repository;

import com.sanly.dmv.entity.IssuanceAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssuanceAuditLogRepository extends JpaRepository<IssuanceAuditLog, Long> {
}
