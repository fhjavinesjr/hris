package com.humanresource.services;

import com.humanresource.dtos.OfficialEngagementApplicationDTO;

import java.util.List;

public interface OfficialEngagementApplicationService {

    OfficialEngagementApplicationDTO create(OfficialEngagementApplicationDTO dto) throws Exception;

    List<OfficialEngagementApplicationDTO> getAll() throws Exception;

    List<OfficialEngagementApplicationDTO> getAllByEmployeeId(Long employeeId) throws Exception;

    List<OfficialEngagementApplicationDTO> getPendingAll() throws Exception;

    OfficialEngagementApplicationDTO approve(Long id, Long approvedById, String remarks) throws Exception;

    OfficialEngagementApplicationDTO disapprove(Long id, Long approvedById, String remarks) throws Exception;

    OfficialEngagementApplicationDTO update(Long id, OfficialEngagementApplicationDTO dto) throws Exception;

    Boolean delete(Long id) throws Exception;
}
