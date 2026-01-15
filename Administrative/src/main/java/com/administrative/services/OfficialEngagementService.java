package com.administrative.services;

import com.administrative.dtos.OfficialEngagementDTO;

import java.util.List;

public interface OfficialEngagementService {

    OfficialEngagementDTO createOfficialEngagement(OfficialEngagementDTO officialEngagementDTO) throws Exception;

    List<OfficialEngagementDTO> getAllOfficialEngagement() throws Exception;

    OfficialEngagementDTO getOfficialEngagementById(Long officialEngagementId) throws Exception;

    OfficialEngagementDTO updateOfficialEngagement(Long officialEngagementId, OfficialEngagementDTO officialEngagementDTO) throws Exception;

    Boolean deleteOfficialEngagement(Long officialEngagementId) throws Exception;

}
