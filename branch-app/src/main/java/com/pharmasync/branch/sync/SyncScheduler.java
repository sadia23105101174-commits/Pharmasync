package com.pharmasync.branch.sync;

import com.pharmasync.branch.entity.Outbox;
import com.pharmasync.branch.repository.OutboxRepository;
import com.pharmasync.domain.sync.SyncBatchResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled sync job. Runs on a fixed delay and delegates to BranchSyncClient.
 * Failures are logged and swallowed so the scheduler keeps running on the next
 * cycle; the client retries internally with exponential backoff.
 */
@Component
public class SyncScheduler {

    private final BranchSyncClient syncClient;
    private final OutboxRepository outboxRepository;
    private final SyncClientProperties props;

    public SyncScheduler(BranchSyncClient syncClient,
                         OutboxRepository outboxRepository,
                         SyncClientProperties props) {
        this.syncClient = syncClient;
        this.outboxRepository = outboxRepository;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${pharmasync.sync.fixed-delay-ms:30000}")
    public void scheduledSync() {
        try {
            syncClient.sync();
        } catch (Exception e) {
            // Logged by caller via async error handler; keep scheduler alive.
            System.err.println("[pharmasync] sync cycle failed: " + e.getMessage());
        }
    }
}
