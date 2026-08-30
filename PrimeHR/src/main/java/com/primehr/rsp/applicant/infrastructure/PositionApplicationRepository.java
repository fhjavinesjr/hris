package com.primehr.rsp.applicant.infrastructure;

import com.primehr.rsp.applicant.domain.PositionApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PositionApplicationRepository extends JpaRepository<PositionApplication, String> {
    Page<PositionApplication> findByAgencyIdAndApplicantIdOrderByCreatedAtDesc(
            String agencyId, String applicantId, Pageable pageable);
    Page<PositionApplication> findByAgencyIdOrderByCreatedAtDesc(String agencyId, Pageable pageable);
    Optional<PositionApplication> findByIdAndAgencyIdAndApplicantId(String id, String agencyId, String applicantId);
    Optional<PositionApplication> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndApplicantIdAndPublicationIdAndStatusIn(String agencyId, String applicantId,
                                                                      String publicationId,
                                                                      Collection<PositionApplication.Status> statuses);
    Optional<PositionApplication> findFirstByAgencyIdAndApplicantIdAndPublicationIdOrderByApplicationVersionDesc(
            String agencyId, String applicantId, String publicationId);
}
