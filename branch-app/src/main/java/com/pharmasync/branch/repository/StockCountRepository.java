package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.StockCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCountRepository extends JpaRepository<StockCount, String> {
    List<StockCount> findByBatchId(String batchId);
}
