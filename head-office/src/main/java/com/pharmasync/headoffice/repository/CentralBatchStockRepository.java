package com.pharmasync.headoffice.repository;

import com.pharmasync.headoffice.entity.CentralBatchStock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CentralBatchStockRepository extends JpaRepository<CentralBatchStock, Long> {
    Optional<CentralBatchStock> findByBranchIdAndMedicineIdAndBatchNumber(String branchId, String medicineId, String batchNumber);

    Page<CentralBatchStock> findByBranchId(String branchId, Pageable pageable);

    Page<CentralBatchStock> findAll(Pageable pageable);
}
