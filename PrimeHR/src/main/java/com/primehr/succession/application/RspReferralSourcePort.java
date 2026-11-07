package com.primehr.succession.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

public interface RspReferralSourcePort {
    Optional<VacancySnapshot> findVacancy(String agencyId, String publicationId);

    record VacancySnapshot(
            String publicationId,
            String vacancyRequestId,
            String status,
            Long plantillaId,
            String plantillaName,
            Long jobPositionId,
            String jobPositionName,
            Long businessUnitId,
            String businessUnitName,
            String positionProfileId,
            int positionProfileVersion,
            LocalDate openingDate,
            LocalDate closingDate,
            long recordVersion,
            String sourceFingerprint,
            Instant fetchedAt) {
    }
}
