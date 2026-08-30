package com.primehr.integration.humanresource;

import java.time.Instant;
import java.time.LocalDateTime;

public record HumanResourcePlantillaOccupancy(Long plantillaId,boolean occupied,Long activeAppointmentId,
        LocalDateTime assumptionToDutyDate,String sourceFingerprint,Instant fetchedAt) {}
