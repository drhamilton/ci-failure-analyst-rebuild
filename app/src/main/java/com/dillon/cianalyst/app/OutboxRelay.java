package com.dillon.cianalyst.app;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.dillon.cianalyst.core.AnalysisResultStore;
import com.dillon.cianalyst.core.Notifier;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final AnalysisResultStore store;
    private final Notifier notifier;

    @Scheduled(fixedDelay = 5000)
    public void processPending() {
        for (OutboxEntity row : outboxRepository.findByStatus(OutboxStatus.PENDING)) {
            try {
                store.findById(row.referenceId).ifPresent(notifier::send);
                row.status = OutboxStatus.SENT;
            } catch (Exception e) {
                row.attempts++;
            }
            outboxRepository.save(row);
        }
    }
}
