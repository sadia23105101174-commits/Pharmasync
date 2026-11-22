package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.SaleRequest;
import com.pharmasync.branch.dto.SaleResponse;
import com.pharmasync.branch.entity.Sale;
import com.pharmasync.branch.exception.ResourceNotFoundException;
import com.pharmasync.branch.repository.SaleRepository;
import com.pharmasync.branch.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@Tag(name = "Sales", description = "Branch point-of-sale checkout operations")
public class SaleApiController {

    private final SaleService saleService;
    private final SaleRepository saleRepository;

    public SaleApiController(SaleService saleService, SaleRepository saleRepository) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
    }

    @PostMapping
    @Operation(summary = "Process a sale", description = "Allocates stock using FEFO, checks prescription requirements, prints receipt, and writes to append-only operation log and outbox")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
        Sale sale = saleService.processSale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleResponse.fromEntity(sale));
    }

    @GetMapping
    @Operation(summary = "List sales", description = "Returns a paginated list of sales recorded at this branch")
    public ResponseEntity<Page<SaleResponse>> getSales(Pageable pageable) {
        Page<SaleResponse> page = saleRepository.findAll(pageable).map(SaleResponse::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sale by ID", description = "Fetches a single sale and its allocation lines")
    public ResponseEntity<SaleResponse> getSaleById(@PathVariable String id) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale not found with ID: " + id));
        return ResponseEntity.ok(SaleResponse.fromEntity(sale));
    }
}
