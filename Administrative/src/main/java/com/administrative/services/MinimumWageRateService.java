package com.administrative.services;

import com.administrative.dtos.MinimumWageRateDTO;
import java.util.List;

public interface MinimumWageRateService {
    List<MinimumWageRateDTO> getAll();
    MinimumWageRateDTO create(MinimumWageRateDTO request);
    MinimumWageRateDTO update(Long id, MinimumWageRateDTO request);
    void delete(Long id);
}
