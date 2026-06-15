package com.humanresource.services;

import com.humanresource.dtos.LeaveProcessBatchStartResponseDTO;
import com.humanresource.dtos.LeaveProcessJobStatusDTO;
import com.humanresource.dtos.LeaveProcessQueueItemDTO;
import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;

import java.util.List;

public interface LeaveProcessBatchService {

    LeaveProcessBatchStartResponseDTO startBatch(LeaveProcessRequestDTO request);

    LeaveProcessJobStatusDTO getJobStatus(String jobId);

    List<LeaveProcessQueueItemDTO> getJobQueue(String jobId, int fromSeqNo);

    LeaveProcessResultDTO getJobResult(String jobId);
}
