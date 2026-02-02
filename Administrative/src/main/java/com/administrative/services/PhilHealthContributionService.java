package com.administrative.services;

import com.administrative.dtos.PhilHealthContributionDTO;

import java.util.List;

public interface PhilHealthContributionService {

    PhilHealthContributionDTO createPhilHealthContribution(PhilHealthContributionDTO philHealthContributionDTO) throws Exception;

    List<PhilHealthContributionDTO> getAllPhilHealthContribution() throws Exception;

    PhilHealthContributionDTO getPhilHealthContributionById(Long philHealthContributionId) throws Exception;

    PhilHealthContributionDTO updatePhilHealthContribution(Long philHealthContributionId, PhilHealthContributionDTO philHealthContributionDTO) throws Exception;

    Boolean deletePhilHealthContribution(Long philHealthContributionId) throws Exception;

}
