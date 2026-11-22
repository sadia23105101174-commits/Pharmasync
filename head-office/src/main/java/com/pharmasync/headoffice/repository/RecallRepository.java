package com.pharmasync.headoffice.repository;

import com.pharmasync.headoffice.entity.Recall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecallRepository extends JpaRepository<Recall, Long> {
    Optional<Recall> findByBatchNumber(String batchNumber);
}
