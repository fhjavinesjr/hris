package com.timekeeping.services;

import com.timekeeping.dtos.AdmsSyncResultDTO;
import com.timekeeping.entitymodels.AdmsPunchLog;

import java.util.List;
import java.util.Map;

public interface AdmsSyncService {

    AdmsSyncResultDTO syncNewPunches();

    Map<String, Object> getSyncStatus();

    List<AdmsPunchLog> getUnmappedPunches();
}
