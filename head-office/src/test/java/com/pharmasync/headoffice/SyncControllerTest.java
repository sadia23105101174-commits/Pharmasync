package com.pharmasync.headoffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.domain.sync.SyncBatchRequest;
import com.pharmasync.domain.sync.SyncBatchResponse;
import com.pharmasync.domain.sync.SyncOperationDto;
import com.pharmasync.domain.operation.OperationType;
import com.pharmasync.headoffice.entity.CentralBatchStock;
import com.pharmasync.headoffice.entity.ProcessedOperation;
import com.pharmasync.headoffice.repository.CentralBatchStockRepository;
import com.pharmasync.headoffice.repository.ProcessedOperationRepository;
import com.pharmasync.headoffice.service.SyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class SyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SyncService syncService;

    @Autowired
    private CentralBatchStockRepository stockRepository;

    @Autowired
    private ProcessedOperationRepository processedOperationRepository;

    private SyncBatchRequest singleOpRequest(UUID opId, String branchId, long clock, OperationType type, String payload) {
        return new SyncBatchRequest(List.of(new SyncOperationDto(opId, clock, branchId, Instant.now(), type, payload)));
    }

    @Test
    void duplicateOperationIsAppliedOnce() throws Exception {
        CentralBatchStock stock = new CentralBatchStock("BRANCH-001", "MED-1", "Paracetamol",
                "BATCH-001", null, 100, Instant.now());
        stockRepository.save(stock);

        UUID opId = UUID.randomUUID();
        String payload = """
                {
                  "allocations": [
                    { "medicineId": "MED-1", "batchNumber": "BATCH-001", "quantity": 1 }
                  ]
                }
                """;
        SyncBatchRequest request = singleOpRequest(opId, "BRANCH-001", 10, OperationType.SALE, payload);
        String body = objectMapper.writeValueAsString(request);

        // First submission applies the stock delta.
        String first = mockMvc.perform(post("/api/sync/operations")
                        .with(httpBasic("BRANCH-001", "branch-secret-001"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        SyncBatchResponse firstResponse = objectMapper.readValue(first, SyncBatchResponse.class);
        assertThat(firstResponse.acknowledgedIds()).containsExactly(opId);

        // Duplicate submission is acknowledged but stock is not decremented again.
        String second = mockMvc.perform(post("/api/sync/operations")
                        .with(httpBasic("BRANCH-001", "branch-secret-001"))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        SyncBatchResponse secondResponse = objectMapper.readValue(second, SyncBatchResponse.class);
        assertThat(secondResponse.acknowledgedIds()).containsExactly(opId);

        assertThat(processedOperationRepository.count()).isEqualTo(1);
        CentralBatchStock updated = stockRepository.findById(stock.getId()).orElseThrow();
        assertThat(updated.getQuantityOnHand()).isEqualTo(99);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        UUID opId = UUID.randomUUID();
        SyncBatchRequest request = singleOpRequest(opId, "BRANCH-001", 10, OperationType.SALE, "{}");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/sync/operations")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getStockIsPaginated() throws Exception {
        stockRepository.save(new CentralBatchStock("BRANCH-001", "MED-1", "Paracetamol",
                "BATCH-001", null, 100, Instant.now()));

        mockMvc.perform(get("/api/sync/stock")
                        .with(httpBasic("BRANCH-001", "branch-secret-001"))
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void getSyncStatusIsPaginated() throws Exception {
        syncService.applyBatch(singleOpRequest(UUID.randomUUID(), "BRANCH-001", 5,
                OperationType.SALE, "{\"allocations\":[]}"));

        mockMvc.perform(get("/api/sync/sync-status")
                        .with(httpBasic("BRANCH-001", "branch-secret-001"))
                        .param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
