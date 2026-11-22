package com.pharmasync.headoffice.controller;

import com.pharmasync.headoffice.dto.RecallCreateRequest;
import com.pharmasync.headoffice.service.RecallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/recalls")
@Tag(name = "Recalls", description = "Head-office batch recalls and per-branch stock report")
public class RecallController {

    private final RecallService recallService;

    public RecallController(RecallService recallService) {
        this.recallService = recallService;
    }

    @PostMapping
    @Operation(summary = "Issue a batch recall", description = "Marks a batch recalled at head office")
    public ResponseEntity<Void> createRecall(@RequestBody RecallCreateRequest request) {
        recallService.createRecall(
                request.batchNumber(), request.medicineId(),
                request.recallTime() != null ? request.recallTime() : Instant.now(),
                request.reason()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/report/{batchNumber}")
    @Operation(summary = "Recall stock report", description = "Quantity on hand per branch for the recalled batch")
    public ResponseEntity<Map<String, Integer>> recallReport(
            @Parameter(description = "Batch number") @PathVariable String batchNumber) {
        return ResponseEntity.ok(recallService.stockPerBranchForRecall(batchNumber));
    }
}
