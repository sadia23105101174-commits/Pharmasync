package com.pharmasync.branch.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.domain.operation.OperationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;

@SpringBootTest
class BranchSyncClientTest {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private SyncClientProperties syncClientProperties;

    @Autowired
    private ObjectMapper objectMapper;

    private void seedPending(UUID... ids) {
        for (int i = 0; i < ids.length; i++) {
            Outbox outbox = new Outbox(ids[i], i + 1, "BRANCH-001", Instant.now(),
                    OperationType.SALE, "{\"allocations\":[]}", Outbox.STATUS_PENDING);
            outboxRepository.save(outbox);
        }
    }

    private void configureProperties() {
        syncClientProperties.setServerUrl("http://mock-head-office");
        syncClientProperties.setBatchSize(10);
        syncClientProperties.setMaxRetries(3);
        syncClientProperties.setInitialBackoffMs(1);
        syncClientProperties.setMaxBackoffMs(100);
        syncClientProperties.setDeviceSecret("branch-secret-001");
    }

    private Object[] mockSyncClient() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        BranchSyncClient client = new BranchSyncClient(restClient, outboxRepository,
                syncClientProperties, objectMapper, "BRANCH-001");
        return new Object[]{server, client};
    }

    @Test
    void outboxEntriesMarkedAckedAfterSuccessfulSync() throws Exception {
        configureProperties();

        UUID op1 = UUID.randomUUID();
        UUID op2 = UUID.randomUUID();
        seedPending(op1, op2);

        Object[] ctx = mockSyncClient();
        MockRestServiceServer server = (MockRestServiceServer) ctx[0];
        BranchSyncClient client = (BranchSyncClient) ctx[1];

        server.expect(requestTo("http://mock-head-office/api/sync/operations"))
                .andExpect(header("Authorization", containsString("Basic")))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"acknowledgedIds\":[\"%s\",\"%s\"],\"acknowledgedCount\":2,\"processedAt\":\"%s\"}"
                                .formatted(op1.toString(), op2.toString(), Instant.now())));

        client.sync();

        assertThat(outboxRepository.findByOperationId(op1).orElseThrow().getStatus()).isEqualTo(Outbox.STATUS_ACKED);
        assertThat(outboxRepository.findByOperationId(op2).orElseThrow().getStatus()).isEqualTo(Outbox.STATUS_ACKED);
        server.verify();
    }

    @Test
    void syncRetriesWithExponentialBackoffThenAcksOnSuccess() throws Exception {
        configureProperties();

        UUID op1 = UUID.randomUUID();
        seedPending(op1);

        Object[] ctx = mockSyncClient();
        MockRestServiceServer server = (MockRestServiceServer) ctx[0];
        BranchSyncClient client = (BranchSyncClient) ctx[1];

        server.expect(requestTo("http://mock-head-office/api/sync/operations"))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        server.expect(requestTo("http://mock-head-office/api/sync/operations"))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"acknowledgedIds\":[\"%s\"],\"acknowledgedCount\":1,\"processedAt\":\"%s\"}"
                                .formatted(op1.toString(), Instant.now())));

        client.sync();

        assertThat(outboxRepository.findByOperationId(op1).orElseThrow().getStatus()).isEqualTo(Outbox.STATUS_ACKED);
        server.verify();
    }
}