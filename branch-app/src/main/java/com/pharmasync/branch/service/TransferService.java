package com.pharmasync.branch.service;

import com.pharmasync.branch.entity.Transfer;
import com.pharmasync.branch.entity.TransferState;
import com.pharmasync.branch.repository.TransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages branch-to-branch transfers through DISPATCHED, IN_TRANSIT and RECEIVED
 * states. A quantity difference at receipt is flagged.
 */
@Service
public class TransferService {

    private final TransferRepository transferRepository;

    public TransferService(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Transfer createTransfer(String sourceBranchId, String destBranchId,
                                   String batchId, int quantity) {
        Transfer transfer = new Transfer(
                UUID.randomUUID().toString(), sourceBranchId, destBranchId,
                batchId, quantity, Instant.now()
        );
        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer markInTransit(String transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        transfer.setState(TransferState.IN_TRANSIT);
        return transferRepository.save(transfer);
    }

    @Transactional
    public Transfer receiveTransfer(String transferId, int receivedQuantity) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        transfer.setState(TransferState.RECEIVED);
        transfer.setReceivedAt(Instant.now());
        transfer.setReceivedQuantity(receivedQuantity);
        transfer.setQuantityDifference(receivedQuantity != transfer.getQuantity());
        return transferRepository.save(transfer);
    }
}
