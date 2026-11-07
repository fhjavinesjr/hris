package com.administrative.services;

import com.administrative.dtos.BenefitTaxProfileDTO;
import java.util.List;

public interface BenefitTaxProfileService {
    List<BenefitTaxProfileDTO> list(int taxYear);
    BenefitTaxProfileDTO save(Long id, BenefitTaxProfileDTO request);
}
