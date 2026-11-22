package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, String> {
    Optional<Sale> findByReceiptNumber(String receiptNumber);

    List<Sale> findTop5ByOrderBySaleTimeDesc();
}
