package com.sanly.appointments.repository;

import com.sanly.appointments.entity.AvailabilityException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AvailabilityExceptionRepository extends JpaRepository<AvailabilityException, Long> {

    Optional<AvailabilityException> findByOfficeCodeAndExceptionDate(String officeCode, LocalDate exceptionDate);

    List<AvailabilityException> findByOfficeCodeAndExceptionDateBetween(
            String officeCode, LocalDate from, LocalDate to);
}
