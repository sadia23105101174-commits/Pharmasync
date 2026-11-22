package com.pharmasync.branch.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.domain.sync.SyncBatchRequest;
import com.pharmasync.domain.sync.SyncBatchResponse;
import com.pharmasync.domain.sync.SyncOperationDto;
import com.pharmasync.domain.operation.OperationType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Branch sync client. Sends pending outbox operations to head-office in small
 * batches, retries with exponential backoff, and marks outbox entries acked only
 * after head-office acknowledges them. Acked entries are never re-sent because
 * only {@code status = pending} rows are fetched.
 */
@Service
public class BranchSyncClient {

    private final RestClient restClient;
    private final OutboxRepository outboxRepository;
    private final SyncClientProperties props;
    private final ObjectMapper objectMapper;
    private final String branchId;

    public BranchSyncClient(RestClient restClient,
                            OutboxRepository outboxRepository,
                            SyncClientProperties props,
                            ObjectMapper objectMapper,
                            @Value("${branch.id:BRANCH-001}") String branchId) {
        this.restClient = restClient;
        this.outboxRepository = outboxRepository;
        this.props = props;
        this.objectMapper = objectMapper;
        this.branchId = branchId;
    }

    /**
     * Run one sync cycle: fetch pending outbox, send to head-office with retry,
     * acknowledge returned entries locally.
     */
    public void sync() {
        Pageable page = PageRequest.of(0, props.getBatchSize(), Sort.by("lamportClock").ascending());
        Page<Outbox> pendingPage = outboxRepository.findByStatusOrderByLamportClockAsc(Outbox.STATUS_PENDING, page);
        List<Outbox> pending = pendingPage.getContent();
        if (pending.isEmpty()) {
            return;
        }

        SyncBatchRequest request = toRequest(pending);
        SyncBatchResponse response = sendWithRetry(request);

        Set<UUID> ackedIds = new HashSet<>(response.acknowledgedIds());
        List<Outbox> toAck = new ArrayList<>();
        for (Outbox entry : pending) {
            if (ackedIds.contains(entry.getOperationId())) {
                entry.markAcked();
                toAck.add(entry);
            }
        }
        if (!toAck.isEmpty()) {
            outboxRepository.saveAll(toAck);
        }
    }

    private SyncBatchResponse sendWithRetry(SyncBatchRequest request) {
        long backoff = props.getInitialBackoffMs();
        RuntimeException last = null;
        for (int attempt = 0; attempt < props.getMaxRetries(); attempt++) {
            try {
                return restClient.post()
                        .uri(props.getServerUrl() + "/api/sync/operations")
                        .headers(h -> h.setBasicAuth(branchId, props.getDeviceSecret()))
                        .body(request)
                        .retrieve()
                        .body(SyncBatchResponse.class);
            } catch (Exception e) {
                last = new RuntimeException("Sync attempt " + (attempt + 1) + " failed", e);
                if (attempt == props.getMaxRetries() - 1) {
                    break;
                }
                sleep(backoff);
                backoff = Math.min(backoff * 2, props.getMaxBackoffMs());
            }
        }
        throw last;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SyncClientException("Sync interrupted", e);
        }
    }

    private SyncBatchRequest toRequest(List<Outbox> pending) {
        List<SyncOperationDto> operations = new ArrayList<>();
        for (Outbox entry : pending) {
            OperationType type = entry.getType();
            operations.add(new SyncOperationDto(
                    entry.getOperationId(),
                    entry.getLamportClock(),
                    entry.getBranchId(),
                    entry.getTimestamp() != null ? entry.getTimestamp() : Instant.now(),
                    type,
                    entry.getPayload()
            ));
        }
        return new SyncBatchRequest(operations);
    }
}
