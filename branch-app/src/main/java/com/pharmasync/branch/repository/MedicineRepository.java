package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, String> {
    Optional<Medicine> findByBrandNameIgnoreCase(String brandName);
}
