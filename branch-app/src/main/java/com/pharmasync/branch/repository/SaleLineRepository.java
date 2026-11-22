package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.SaleLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SaleLineRepository extends JpaRepository<SaleLine, String> {
    List<SaleLine> findBySale_SaleTimeBetween(Instant start, Instant end);
}
