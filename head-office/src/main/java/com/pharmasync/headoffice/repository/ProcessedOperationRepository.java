package com.pharmasync.headoffice.repository;

import com.pharmasync.headoffice.entity.ProcessedOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcessedOperationRepository extends JpaRepository<ProcessedOperation, Long> {
    boolean existsByOperationId(UUID operationId);

    Optional<ProcessedOperation> findByOperationId(UUID operationId);
}
