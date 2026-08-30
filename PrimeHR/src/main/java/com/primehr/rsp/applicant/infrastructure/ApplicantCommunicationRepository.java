package com.primehr.rsp.applicant.infrastructure;

import com.primehr.rsp.applicant.domain.ApplicantCommunication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicantCommunicationRepository extends JpaRepository<ApplicantCommunication, String> {
    List<ApplicantCommunication> findByAgencyIdAndApplicationIdOrderByOccurredAtAsc(
            String agencyId, String applicationId);
}
