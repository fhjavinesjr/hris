package com.humanresource.repositories;

import com.humanresource.entitymodels.LeaveInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveInformationRepository extends JpaRepository<LeaveInformation, Long> {

    // All records for an employee (ledger view) — oldest first
    List<LeaveInformation> findByEmployeeIdOrderByCutoffStartDateAsc(Long employeeId);

    // Latest record BEFORE a given period end — used to carry forward the previous balance.
    // Must be strictly before (cutoffEndDate < before) to avoid self-referencing the
    // current period when reprocessing an existing record.
    Optional<LeaveInformation> findTopByEmployeeIdAndCutoffEndDateBeforeOrderByCutoffEndDateDesc(
            Long employeeId, LocalDate before);

    // All records for a given period — for bulk period report
    List<LeaveInformation> findByCutoffStartDateAndCutoffEndDate(LocalDate start, LocalDate end);

    // Check if a record already exists for this employee × period
    boolean existsByEmployeeIdAndCutoffStartDateAndCutoffEndDate(Long employeeId, LocalDate start, LocalDate end);

    // Guard: does a LATER period already exist for this employee?
    boolean existsByEmployeeIdAndCutoffEndDateGreaterThan(Long employeeId, LocalDate cutoffEndDate);

    // All records by salary period setting
    List<LeaveInformation> findBySalaryPeriodSettingId(Long salaryPeriodSettingId);

    // Lock / unlock a specific record — service fetches by ID
    Optional<LeaveInformation> findByEmployeeIdAndCutoffStartDateAndCutoffEndDate(
            Long employeeId, LocalDate start, LocalDate end);

    // All records whose cutoffStartDate falls within a calendar year — for the "View All Year" feature
    List<LeaveInformation> findByCutoffStartDateBetweenOrderByCutoffStartDateAsc(
            LocalDate from, LocalDate to);

    /**
     * One latest posted ledger row per employee on or before the requested date.
     * JPQL keeps this query portable between SQL Server and PostgreSQL.
     */
    @Query("select li from LeaveInformation li " +
            "where li.cutoffEndDate = (" +
            "select max(later.cutoffEndDate) from LeaveInformation later " +
            "where later.employeeId = li.employeeId and later.cutoffEndDate <= :asOfDate)")
    List<LeaveInformation> findLatestPostedBalancesAsOf(@Param("asOfDate") LocalDate asOfDate);
}
