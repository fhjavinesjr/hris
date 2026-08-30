package com.primehr.rsp.infrastructure;

import com.primehr.rsp.domain.VacancyPublicationRequirementSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyPublicationRequirementRepository
        extends JpaRepository<VacancyPublicationRequirementSnapshot, String> {
    List<VacancyPublicationRequirementSnapshot> findByPublicationIdAndAgencyIdOrderByDisplayOrderAscIdAsc(
            String publicationId, String agencyId);
}
