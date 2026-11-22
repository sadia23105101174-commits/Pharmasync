package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Batch;
import com.pharmasync.domain.batch.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, String> {
    List<Batch> findByMedicineId(String medicineId);

    List<Batch> findByMedicineIdAndStatus(String medicineId, BatchStatus status);

    Optional<Batch> findByMedicineIdAndBatchNumber(String medicineId, String batchNumber);

    Page<Batch> findByMedicineId(String medicineId, Pageable pageable);
}
