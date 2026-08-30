package com.g4.ems.repository;

import com.g4.ems.domain.MaterialInventory;
import com.g4.ems.domain.enums.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialInventoryRepository extends JpaRepository<MaterialInventory, Long> {
    Optional<MaterialInventory> findByProjectIdAndMaterialType(Long projectId, MaterialType materialType);

    List<MaterialInventory> findByProjectId(Long projectId);
}
