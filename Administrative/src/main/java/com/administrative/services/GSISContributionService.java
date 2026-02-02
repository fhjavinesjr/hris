package com.administrative.services;

import com.administrative.dtos.GSISContributionDTO;

import java.util.List;

public interface GSISContributionService {

    GSISContributionDTO createGSISContribution(GSISContributionDTO gsisContributionDTO) throws Exception;

    List<GSISContributionDTO> getAllGSISContribution() throws Exception;

    GSISContributionDTO getGSISContributionById(Long gsisContributionId) throws Exception;

    GSISContributionDTO updateGSISContribution(Long gsisContributionId, GSISContributionDTO gsisContributionDTO) throws Exception;

    Boolean deleteGSISContribution(Long gsisContributionId) throws Exception;

}
