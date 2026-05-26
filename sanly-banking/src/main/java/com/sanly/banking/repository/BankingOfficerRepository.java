package com.sanly.banking.repository;

import com.sanly.banking.entity.BankingOfficer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankingOfficerRepository extends JpaRepository<BankingOfficer, UUID> {

    Optional<BankingOfficer> findByUsernameAndActiveTrue(String username);
}
