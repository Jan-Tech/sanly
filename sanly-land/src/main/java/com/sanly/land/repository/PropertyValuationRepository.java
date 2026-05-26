package com.sanly.land.repository;

import com.sanly.land.entity.PropertyValuation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PropertyValuationRepository extends JpaRepository<PropertyValuation, UUID> {
    List<PropertyValuation> findByCadastralNumberOrderByValuationDateDesc(String cadastralNumber);
}
