package com.primehr.rsp.applicant.infrastructure;

import com.primehr.rsp.applicant.domain.ApplicationDocumentSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationDocumentSnapshotRepository extends JpaRepository<ApplicationDocumentSnapshot, String> {
    List<ApplicationDocumentSnapshot> findByAgencyIdAndApplicationIdOrderByDisplayOrderAsc(
            String agencyId, String applicationId);
    Optional<ApplicationDocumentSnapshot> findByIdAndAgencyIdAndApplicationId(
            String id, String agencyId, String applicationId);
    void deleteByAgencyIdAndApplicationId(String agencyId, String applicationId);
}
