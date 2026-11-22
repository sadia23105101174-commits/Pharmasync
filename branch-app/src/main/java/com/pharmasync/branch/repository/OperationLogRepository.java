package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    Optional<OperationLog> findByOperationId(UUID operationId);
}
