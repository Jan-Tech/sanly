package com.sanly.customs.repository;

import com.sanly.customs.entity.CargoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CargoItemRepository extends JpaRepository<CargoItem, UUID> {
    List<CargoItem> findByDeclarationCode(String declarationCode);
}
