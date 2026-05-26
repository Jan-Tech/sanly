package com.sanly.notifications.repository;

import com.sanly.notifications.entity.ServiceAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServiceAccountRepository extends JpaRepository<ServiceAccount, UUID> {
    Optional<ServiceAccount> findByServiceNameAndActiveTrue(String serviceName);
}
