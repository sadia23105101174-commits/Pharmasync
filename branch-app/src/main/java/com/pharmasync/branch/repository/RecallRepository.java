package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Recall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecallRepository extends JpaRepository<Recall, String> {
    List<Recall> findByBatchId(String batchId);

    Optional<Recall> findByBatchIdAndRecallTimeBefore(String batchId, java.time.Instant moment);

    List<Recall> findByApplied(boolean applied);
}
