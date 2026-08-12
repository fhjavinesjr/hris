package com.timekeeping.services;

import com.timekeeping.entitymodels.DTRDaily;
import com.timekeeping.entitymodels.DTRSegment;
import com.timekeeping.repositories.DTRDailyRepository;
import com.timekeeping.repositories.DTRSegmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class DtrTransactionDeleteService {

    private final DTRDailyRepository dtrDailyRepository;
    private final DTRSegmentRepository dtrSegmentRepository;
    private final JdbcTemplate jdbcTemplate;

    public DtrTransactionDeleteService(
            DTRDailyRepository dtrDailyRepository,
            DTRSegmentRepository dtrSegmentRepository,
            JdbcTemplate jdbcTemplate) {
        this.dtrDailyRepository = dtrDailyRepository;
        this.dtrSegmentRepository = dtrSegmentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Physically deletes the selected DTR segment. If it was the final segment,
     * the parent dtr_daily row is physically deleted too. ADMS punch logs are
     * deliberately left untouched so a later Search can rebuild the DTR.
     */
    @Transactional
    public void deleteSegmentAndEmptyDaily(Long segmentId) {
        DTRSegment segment = dtrSegmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DTR segment not found"));

        DTRDaily daily = segment.getDtrDaily();
        if (daily == null || daily.getDtrDailyId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "DTR segment has no parent daily record");
        }

        DTRDaily managedDaily = dtrDailyRepository.findById(daily.getDtrDailyId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "DTR daily record not found"));

        // Preserve legacy/manual raw logs while removing their foreign-key link
        // to the segment. ADMS punch logs are intentionally not changed.
        jdbcTemplate.update(
                "UPDATE dtr_raw_log SET dtr_segment_id = NULL, is_processed = ? WHERE dtr_segment_id = ?",
                Boolean.FALSE,
                segmentId
        );

        boolean removed = managedDaily.getSegments().removeIf(existing ->
                Objects.equals(existing.getDtrSegmentId(), segmentId));

        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DTR segment not found in daily record");
        }

        if (managedDaily.getSegments().isEmpty()) {
            // Cascade + orphanRemoval physically removes the final segment and daily.
            dtrDailyRepository.delete(managedDaily);
            dtrDailyRepository.flush();
            return;
        }

        renumberSegments(managedDaily.getSegments());
        recomputeTotals(managedDaily);
        dtrDailyRepository.saveAndFlush(managedDaily);
    }

    private void renumberSegments(List<DTRSegment> segments) {
        segments.sort(Comparator
                .comparing(DTRSegment::getSegmentNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(DTRSegment::getDtrSegmentId, Comparator.nullsLast(Long::compareTo)));

        for (int index = 0; index < segments.size(); index++) {
            segments.get(index).setSegmentNo(index + 1);
        }
    }

    private void recomputeTotals(DTRDaily daily) {
        int work = 0;
        int late = 0;
        int under = 0;
        int over = 0;

        for (DTRSegment segment : daily.getSegments()) {
            work += safe(segment.getWorkMinutes());
            late += safe(segment.getLateMinutes());
            under += safe(segment.getUndertimeMinutes());
            over += safe(segment.getOvertimeMinutes());
        }

        daily.setTotalWorkMinutes(work);
        daily.setTotalLateMinutes(late);
        daily.setTotalUndertimeMinutes(under);
        daily.setTotalOvertimeMinutes(over);
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }
}
