package com.administrative.services;

import com.administrative.dtos.PrimeHrReportSourceDtos.Branding;
import com.administrative.dtos.PrimeHrReportSourceDtos.Position;

public interface PrimeHrReportSourceService {
    Branding branding();
    Position position(Long plantillaId);
}
