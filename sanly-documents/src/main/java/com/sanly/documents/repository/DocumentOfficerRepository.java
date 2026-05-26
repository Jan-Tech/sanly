package com.sanly.documents.repository;

import com.sanly.documents.entity.DocumentOfficer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentOfficerRepository extends JpaRepository<DocumentOfficer, Long> {

    Optional<DocumentOfficer> findByUsername(String username);
}
