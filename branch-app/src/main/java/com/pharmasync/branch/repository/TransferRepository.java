package com.pharmasync.branch.repository;

import com.pharmasync.branch.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, String> {
    List<Transfer> findByDestBranchId(String destBranchId);

    List<Transfer> findByState(com.pharmasync.branch.entity.TransferState state);
}
