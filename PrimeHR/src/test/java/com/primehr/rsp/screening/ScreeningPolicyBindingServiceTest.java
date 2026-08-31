package com.primehr.rsp.screening;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primehr.rsp.domain.VacancyPublication;
import com.primehr.rsp.infrastructure.VacancyPublicationRepository;
import com.primehr.rsp.screening.api.ScreeningPolicyDtos.BindPolicy;
import com.primehr.rsp.screening.application.*;
import com.primehr.rsp.screening.infrastructure.*;
import com.primehr.shared.audit.PrimeHrAuditService;
import com.primehr.shared.exception.*;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScreeningPolicyBindingServiceTest {
    private final ScreeningPolicyRepository policies = mock(ScreeningPolicyRepository.class);
    private final ScreeningCriterionRepository criteria = mock(ScreeningCriterionRepository.class);
    private final ScreeningReasonCodeRepository reasons = mock(ScreeningReasonCodeRepository.class);
    private final PublicationScreeningPolicyRepository bindings = mock(PublicationScreeningPolicyRepository.class);
    private final VacancyPublicationRepository publications = mock(VacancyPublicationRepository.class);
    private final PrimeHrAuditService audit = mock(PrimeHrAuditService.class);
    private final ScreeningPolicyService service = new ScreeningPolicyServiceImpl(policies, criteria, reasons,
            bindings, publications, new ScreeningEvidenceEvaluator(), audit, new ObjectMapper());

    @Test
    void bindingIsAgencyOwnedImmutableAndUsesBothOptimisticVersions() {
        when(publications.findByIdAndAgencyId("publication", "OTHER")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.bind("OTHER", "publication", new BindPolicy("policy", 0L, 0L), null))
                .isInstanceOf(ResourceNotFoundException.class);

        when(bindings.existsByAgencyIdAndPublicationId("AGENCY", "publication")).thenReturn(true);
        assertThatThrownBy(() -> service.bind("AGENCY", "publication", new BindPolicy("policy", 0L, 0L), null))
                .hasMessageContaining("immutable");

        when(bindings.existsByAgencyIdAndPublicationId("AGENCY", "publication")).thenReturn(false);
        VacancyPublication publication = mock(VacancyPublication.class);
        when(publication.getVersion()).thenReturn(4L);
        when(publications.findByIdAndAgencyId("publication", "AGENCY")).thenReturn(Optional.of(publication));
        assertThatThrownBy(() -> service.bind("AGENCY", "publication", new BindPolicy("policy", 0L, 3L), null))
                .isInstanceOf(OptimisticConflictException.class);
        verifyNoInteractions(policies);
    }
}
