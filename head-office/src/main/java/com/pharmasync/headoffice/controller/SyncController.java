package com.pharmasync.headoffice.controller;

import com.pharmasync.domain.sync.SyncBatchRequest;
import com.pharmasync.domain.sync.SyncBatchResponse;
import com.pharmasync.domain.sync.SyncStatusDto;
import com.pharmasync.headoffice.entity.BranchSyncStatus;
import com.pharmasync.headoffice.entity.CentralBatchStock;
import com.pharmasync.headoffice.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Head-office sync API.
 * POST /api/sync/operations accepts a batch of operations, idempotent by operation UUID,
 * returns acknowledged ids, updates central stock per batch and tracks per-branch
 * last acknowledged logical clock / last sync time.
 * GET endpoints expose paginated central stock and sync status.
 */
@RestController
@RequestMapping("/api/sync")
@Tag(name = "Sync", description = "Branch-to-head-office idempotent sync and central stock")
public class SyncController {

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping("/operations")
    @Operation(summary = "Submit a batch of branch operations", description = "Idempotent by operation UUID; returns acknowledged ids and updates central stock per batch")
    public ResponseEntity<SyncBatchResponse> submitOperations(@Parameter(hidden = true) @RequestBody @Valid SyncBatchRequest request) {
        SyncBatchResponse response = syncService.applyBatch(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stock")
    @Operation(summary = "List central stock", description = "Paginated central stock per product and batch; optionally filtered by branch")
    public ResponseEntity<Page<CentralBatchStock>> getStock(
            @Parameter(description = "Filter by branch id") @RequestParam(required = false) String branchId,
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(syncService.getStock(branchId, pageable));
    }

    @GetMapping("/sync-status")
    @Operation(summary = "List branch sync status", description = "Paginated sync status for all branches")
    public ResponseEntity<Page<BranchSyncStatus>> getSyncStatus(
            @Parameter(hidden = true) @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(syncService.getSyncStatus(pageable));
    }

    @GetMapping("/sync-status/{branchId}")
    @Operation(summary = "Get sync status for a branch", description = "Last acknowledged logical clock, last sync time and total operations processed")
    public ResponseEntity<SyncStatusDto> getSyncStatus(
            @Parameter(description = "Branch id") @PathVariable String branchId) {
        return ResponseEntity.ok(syncService.getSyncStatus(branchId));
    }
}
