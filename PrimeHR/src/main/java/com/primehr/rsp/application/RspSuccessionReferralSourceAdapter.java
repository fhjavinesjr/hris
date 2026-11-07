package com.primehr.rsp.application;

import com.primehr.rsp.infrastructure.VacancyPublicationRepository;
import com.primehr.succession.application.RspReferralSourcePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Component
public class RspSuccessionReferralSourceAdapter implements RspReferralSourcePort {
    private final VacancyPublicationRepository publications;

    public RspSuccessionReferralSourceAdapter(VacancyPublicationRepository publications) {
        this.publications = publications;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VacancySnapshot> findVacancy(String agencyId, String publicationId) {
        return publications.findByIdAndAgencyId(publicationId, agencyId).map(v -> new VacancySnapshot(
                v.getId(), v.getVacancyRequest().getId(), v.getStatus().name(), v.getPlantillaId(),
                v.getPlantillaName(), v.getJobPositionId(), v.getJobPositionName(), v.getBusinessUnitId(),
                v.getBusinessUnitName(), v.getPositionProfileId(), v.getPositionProfileDefinitionVersion(),
                v.getOpeningDate(), v.getClosingDate(), v.getVersion(),
                v.getAdministrativeFingerprint() + ":" + v.getHrmFingerprint() + ":" + v.getVersion(),
                Instant.now()));
    }
}
