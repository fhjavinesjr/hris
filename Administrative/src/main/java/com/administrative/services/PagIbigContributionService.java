package com.administrative.services;

import com.administrative.dtos.PagIbigContributionDTO;

import java.util.List;

public interface PagIbigContributionService {

    PagIbigContributionDTO createPagIbigContribution(PagIbigContributionDTO dto) throws Exception;

    List<PagIbigContributionDTO> getAllPagIbigContribution() throws Exception;

    PagIbigContributionDTO getCurrent() throws Exception;

    PagIbigContributionDTO updatePagIbigContribution(Long id, PagIbigContributionDTO dto) throws Exception;

    Boolean deletePagIbigContribution(Long id) throws Exception;
}
