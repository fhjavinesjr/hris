package com.timekeeping.repositories;

import com.timekeeping.entitymodels.AdmsPunchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdmsPunchLogRepository extends JpaRepository<AdmsPunchLog, Long> {

    boolean existsByAdmsCheckoutId(Long admsCheckoutId);

    long countByImportStatus(String importStatus);

    long countByImportStatusAndDtrProcessedFalse(String importStatus);

    Optional<AdmsPunchLog> findTopByOrderByImportedAtDesc();

    List<AdmsPunchLog> findTop100ByImportStatusOrderByCheckTimeDesc(String importStatus);

    List<AdmsPunchLog> findByImportStatusAndDtrProcessedFalseAndEmployeeIdIsNotNullOrderByEmployeeIdAscCheckTimeAscAdmsPunchLogIdAsc(
            String importStatus
    );

    List<AdmsPunchLog> findByImportStatusAndEmployeeIdAndCheckTimeBetweenOrderByCheckTimeAscAdmsPunchLogIdAsc(
            String importStatus,
            String employeeId,
            LocalDateTime from,
            LocalDateTime to
    );
}
