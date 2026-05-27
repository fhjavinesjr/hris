package com.payroll.services;

import com.payroll.dtos.PayrollComputationRequest;
import com.payroll.dtos.PayrollComputationResponse;
import com.payroll.dtos.PayrollJobStatusDTO;
import com.payroll.dtos.PayrollQueueItemDTO;
import java.util.List;

public interface PayrollBatchService {

    /**
     * Trigger an async batch payroll computation.
     * Returns immediately with a jobId; the caller polls
     * {@link #getJobStatus(String)} for progress.
     *
     * @param request computation parameters (period, filters, options)
     * @param authToken JWT token forwarded to downstream services for bulk data fetch
     */
    PayrollComputationResponse startBatch(PayrollComputationRequest request, String authToken);

    /**
     * Poll the current status and progress of an async batch job.
     *
     * @param jobId UUID returned by {@link #startBatch}
     */
    PayrollJobStatusDTO getJobStatus(String jobId);

    /**
     * Returns per-employee queue items for a batch job, starting from {@code fromSeqNo}.
     * Use {@code fromSeqNo=0} on the first call; pass the last received seqNo + 1 on
     * subsequent calls for incremental polling (no duplicates).
     *
     * @param jobId     UUID of the batch job
     * @param fromSeqNo inclusive lower bound of sequence numbers to return
     */
    List<PayrollQueueItemDTO> getJobQueue(String jobId, int fromSeqNo);
}
