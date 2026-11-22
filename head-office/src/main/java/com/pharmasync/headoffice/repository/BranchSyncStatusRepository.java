package com.pharmasync.headoffice.repository;

import com.pharmasync.headoffice.entity.BranchSyncStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchSyncStatusRepository extends JpaRepository<BranchSyncStatus, String> {
    Optional<BranchSyncStatus> findByBranchId(String branchId);

    Page<BranchSyncStatus> findAll(Pageable pageable);
}
