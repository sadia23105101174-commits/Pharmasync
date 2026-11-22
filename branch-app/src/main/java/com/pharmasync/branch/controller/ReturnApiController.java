package com.pharmasync.branch.controller;

import com.pharmasync.branch.dto.ReturnRequest;
import com.pharmasync.branch.dto.ReturnResponse;
import com.pharmasync.branch.entity.Return;
import com.pharmasync.branch.repository.ReturnRepository;
import com.pharmasync.branch.service.ReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/returns")
@Tag(name = "Returns", description = "Customer return processing and stock restoration")
public class ReturnApiController {

    private final ReturnService returnService;
    private final ReturnRepository returnRepository;

    public ReturnApiController(ReturnService returnService, ReturnRepository returnRepository) {
        this.returnService = returnService;
        this.returnRepository = returnRepository;
    }

    @PostMapping
    @Operation(summary = "Process a return", description = "Restores batch quantity on hand, persists return record, and logs domain Operation to log and outbox")
    public ResponseEntity<ReturnResponse> createReturn(@Valid @RequestBody ReturnRequest request) {
        Return returnEntity = returnService.processReturn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReturnResponse.fromEntity(returnEntity));
    }

    @GetMapping
    @Operation(summary = "List returns", description = "Returns a paginated list of medicine returns")
    public ResponseEntity<Page<ReturnResponse>> getReturns(Pageable pageable) {
        Page<ReturnResponse> page = returnRepository.findAll(pageable).map(ReturnResponse::fromEntity);
        return ResponseEntity.ok(page);
    }
}
