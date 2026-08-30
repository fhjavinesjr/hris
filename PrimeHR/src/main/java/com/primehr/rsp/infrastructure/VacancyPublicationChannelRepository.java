package com.primehr.rsp.infrastructure;

import com.primehr.rsp.domain.VacancyPublicationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VacancyPublicationChannelRepository
        extends JpaRepository<VacancyPublicationChannel, String> {
    List<VacancyPublicationChannel> findByPublicationIdAndAgencyIdOrderByPublicationDateAscChannelNameAsc(
            String publicationId, String agencyId);
}
