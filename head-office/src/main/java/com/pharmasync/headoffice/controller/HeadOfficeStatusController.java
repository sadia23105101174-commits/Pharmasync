package com.pharmasync.headoffice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/headoffice")
@Tag(name = "Head Office System", description = "Central management and sync hub status")
public class HeadOfficeStatusController {

    @GetMapping("/status")
    @Operation(summary = "Get head office status", description = "Returns operational status of central head office hub")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "head-office-central",
                "timestamp", Instant.now().toString()
        ));
    }
}
