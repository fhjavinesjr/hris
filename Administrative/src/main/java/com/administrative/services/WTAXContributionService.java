package com.administrative.services;

import com.administrative.dtos.WTAXContributionDTO;

import java.util.List;

public interface WTAXContributionService {

    WTAXContributionDTO createWTAXContribution(WTAXContributionDTO wTAXContributionDTO) throws Exception;

    List<WTAXContributionDTO> getAllWTAXContribution() throws Exception;

    WTAXContributionDTO getWTAXContributionById(Long wTAXContributionId) throws Exception;

    WTAXContributionDTO updateWTAXContribution(Long wTAXContributionId, WTAXContributionDTO wTAXContributionDTO) throws Exception;

    Boolean deleteWTAXContribution(Long wTAXContributionId) throws Exception;

}
