package com.pharmasync.headoffice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.headoffice.dto.RecallCreateRequest;
import com.pharmasync.headoffice.entity.CentralBatchStock;
import com.pharmasync.headoffice.entity.Recall;
import com.pharmasync.headoffice.repository.CentralBatchStockRepository;
import com.pharmasync.headoffice.repository.RecallRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class HeadOfficeRecallControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CentralBatchStockRepository stockRepository;

    @Autowired
    private RecallRepository recallRepository;

    @Test
    void createRecallAndPerBranchReport() throws Exception {
        stockRepository.save(new CentralBatchStock("BRANCH-001", "MED-1", "Paracetamol",
                "BATCH-001", null, 100, Instant.now()));
        stockRepository.save(new CentralBatchStock("BRANCH-002", "MED-1", "Paracetamol",
                "BATCH-001", null, 50, Instant.now()));

        RecallCreateRequest request = new RecallCreateRequest("BATCH-001", "MED-1", Instant.now(), "Safety recall");
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/v1/recalls")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        assertThat(recallRepository.findByBatchNumber("BATCH-001")).isPresent();

        mockMvc.perform(get("/api/v1/recalls/report/{batchNumber}", "BATCH-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.BRANCH-001").value(100))
                .andExpect(jsonPath("$.BRANCH-002").value(50));
    }

    @Test
    void recallReportMissingBatchReturnsEmptyMap() throws Exception {
        mockMvc.perform(get("/api/v1/recalls/report/{batchNumber}", "NOPE"))
                .andExpect(status().isOk());
    }
}
