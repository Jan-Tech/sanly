package com.sanly.banking.repository;

import com.sanly.banking.entity.RegisteredBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegisteredBankRepository extends JpaRepository<RegisteredBank, UUID> {

    Optional<RegisteredBank> findByBankCode(String bankCode);

    Optional<RegisteredBank> findByBankCodeAndStatus(String bankCode, String status);

    List<RegisteredBank> findByStatus(String status);
}
