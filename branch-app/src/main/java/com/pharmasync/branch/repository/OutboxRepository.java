package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Outbox;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    Optional<Outbox> findByOperationId(UUID operationId);

    List<Outbox> findByStatus(String status);

    List<Outbox> findByStatusOrderByLamportClockAsc(String status);

    Page<Outbox> findByStatusOrderByLamportClockAsc(String status, Pageable pageable);

    Page<Outbox> findByStatus(String status, Pageable pageable);
}
