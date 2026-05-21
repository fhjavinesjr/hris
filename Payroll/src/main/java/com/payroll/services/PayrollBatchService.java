package com.payroll.services;

import com.payroll.dtos.PayrollComputationRequest;
import com.payroll.dtos.PayrollComputationResponse;
import com.payroll.dtos.PayrollJobStatusDTO;

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
}
