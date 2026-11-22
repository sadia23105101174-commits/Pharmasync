package com.pharmasync.branch.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/branch")
@Tag(name = "Branch System", description = "Branch status and diagnostics")
public class BranchStatusController {

    @GetMapping("/status")
    @Operation(summary = "Get branch status", description = "Returns the operational and offline status of this branch node")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "mode", "OFFLINE_READY",
                "timestamp", Instant.now().toString()
        ));
    }
}
