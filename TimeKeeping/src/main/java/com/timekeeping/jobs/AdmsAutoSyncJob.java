package com.timekeeping.jobs;

import com.timekeeping.dtos.AdmsSyncResultDTO;
import com.timekeeping.services.AdmsSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "adms.sync.auto-enabled", havingValue = "true")
public class AdmsAutoSyncJob {

    private static final Logger log = LoggerFactory.getLogger(AdmsAutoSyncJob.class);

    private final AdmsSyncService admsSyncService;

    public AdmsAutoSyncJob(AdmsSyncService admsSyncService) {
        this.admsSyncService = admsSyncService;
    }

    @Scheduled(
            initialDelayString = "${adms.sync.initial-delay-ms:15000}",
            fixedDelayString = "${adms.sync.interval-ms:60000}"
    )
    public void synchronize() {
        try {
            AdmsSyncResultDTO result = admsSyncService.syncNewPunches();
            if (result.getRecordsRead() > 0 || result.getDtrSegmentsCreated() > 0 || result.getDtrConflicts() > 0) {
                log.info(
                        "Automatic ADMS sync completed read={} imported={} segments={} processedPunches={} pending={} conflicts={}",
                        result.getRecordsRead(),
                        result.getImported(),
                        result.getDtrSegmentsCreated(),
                        result.getDtrPunchesProcessed(),
                        result.getDtrPendingPunches(),
                        result.getDtrConflicts()
                );
            }
        } catch (Exception exception) {
            log.error("Automatic ADMS synchronization failed", exception);
        }
    }
}
