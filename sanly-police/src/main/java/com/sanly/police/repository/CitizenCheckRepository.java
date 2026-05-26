package com.sanly.police.repository;

import com.sanly.police.entity.CitizenCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CitizenCheckRepository extends JpaRepository<CitizenCheck, UUID> {
}
