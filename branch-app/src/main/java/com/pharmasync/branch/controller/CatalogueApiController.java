package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.BatchResponse;
import com.pharmasync.branch.dto.MedicineResponse;
import com.pharmasync.branch.repository.BatchRepository;
import com.pharmasync.branch.repository.MedicineRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Catalogue & Batches", description = "Query medicines and active/tracked batches")
public class CatalogueApiController {

    private final MedicineRepository medicineRepository;
    private final BatchRepository batchRepository;

    public CatalogueApiController(MedicineRepository medicineRepository, BatchRepository batchRepository) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
    }

    @GetMapping("/medicines")
    @Operation(summary = "List medicines", description = "Paginated list of medicines registered at branch")
    public ResponseEntity<Page<MedicineResponse>> getMedicines(Pageable pageable) {
        Page<MedicineResponse> page = medicineRepository.findAll(pageable).map(MedicineResponse::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/batches")
    @Operation(summary = "List batches", description = "Paginated list of batches, optionally filtered by medicine ID")
    public ResponseEntity<Page<BatchResponse>> getBatches(
            @RequestParam(required = false) String medicineId,
            Pageable pageable) {
        Page<BatchResponse> page;
        if (medicineId != null && !medicineId.trim().isEmpty()) {
            page = batchRepository.findByMedicineId(medicineId, pageable).map(BatchResponse::fromEntity);
        } else {
            page = batchRepository.findAll(pageable).map(BatchResponse::fromEntity);
        }
        return ResponseEntity.ok(page);
    }
}
