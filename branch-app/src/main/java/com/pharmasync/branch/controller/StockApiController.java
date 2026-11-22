package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.BatchResponse;
import com.pharmasync.branch.dto.ReceiveStockRequest;
import com.pharmasync.branch.dto.StockCountRequest;
import com.pharmasync.branch.dto.StockCountResponse;
import com.pharmasync.branch.entity.Batch;
import com.pharmasync.branch.entity.StockCount;
import com.pharmasync.branch.repository.StockCountRepository;
import com.pharmasync.branch.service.StockCountService;
import com.pharmasync.branch.service.StockReceivingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stock")
@Tag(name = "Stock Operations", description = "Stock receiving and physical stock count operations")
public class StockApiController {

    private final StockReceivingService stockReceivingService;
    private final StockCountService stockCountService;
    private final StockCountRepository stockCountRepository;

    public StockApiController(StockReceivingService stockReceivingService,
                              StockCountService stockCountService,
                              StockCountRepository stockCountRepository) {
        this.stockReceivingService = stockReceivingService;
        this.stockCountService = stockCountService;
        this.stockCountRepository = stockCountRepository;
    }

    @PostMapping("/receive")
    @Operation(summary = "Receive stock", description = "Increases stock for existing batch or creates a new batch, and logs domain Operation")
    public ResponseEntity<BatchResponse> receiveStock(@Valid @RequestBody ReceiveStockRequest request) {
        Batch batch = stockReceivingService.receiveStock(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BatchResponse.fromEntity(batch));
    }

    @PostMapping("/count")
    @Operation(summary = "Record physical stock count", description = "Updates quantity on hand to match physical count, records difference, and logs domain Operation")
    public ResponseEntity<StockCountResponse> recordStockCount(@Valid @RequestBody StockCountRequest request) {
        StockCount count = stockCountService.recordStockCount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(StockCountResponse.fromEntity(count));
    }

    @GetMapping("/counts")
    @Operation(summary = "List stock counts", description = "Returns paginated list of historical stock count adjustments")
    public ResponseEntity<Page<StockCountResponse>> getStockCounts(Pageable pageable) {
        Page<StockCountResponse> page = stockCountRepository.findAll(pageable).map(StockCountResponse::fromEntity);
        return ResponseEntity.ok(page);
    }
}
