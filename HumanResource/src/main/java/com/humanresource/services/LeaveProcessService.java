package com.humanresource.services;

import com.humanresource.dtos.LeaveProcessRequestDTO;
import com.humanresource.dtos.LeaveProcessResultDTO;

public interface LeaveProcessService {

    LeaveProcessResultDTO process(LeaveProcessRequestDTO request) throws Exception;
}
