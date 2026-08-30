package com.primehr.rsp.infrastructure;

import com.primehr.rsp.domain.VacancyPublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import com.primehr.rsp.domain.VacancyPublicationStatus;

public interface VacancyPublicationRepository extends JpaRepository<VacancyPublication, String> {
    Page<VacancyPublication> findByAgencyId(String agencyId, Pageable pageable);
    Optional<VacancyPublication> findByIdAndAgencyId(String id, String agencyId);
    boolean existsByAgencyIdAndVacancyRequestId(String agencyId, String vacancyRequestId);
    List<VacancyPublication> findByAgencyIdAndStatusAndOpeningDateLessThanEqualAndClosingDateGreaterThanEqual(
            String agencyId, VacancyPublicationStatus status, LocalDate openingDate, LocalDate closingDate);
}
