package com.humanresource.integration.primehr;

import java.time.Instant;
import java.time.LocalDateTime;

public record PlantillaOccupancyResponse(Long plantillaId,boolean occupied,Long activeAppointmentId,
        LocalDateTime assumptionToDutyDate,String sourceFingerprint,Instant fetchedAt) {}
