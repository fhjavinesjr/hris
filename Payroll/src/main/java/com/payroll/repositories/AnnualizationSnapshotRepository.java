package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnualizationSnapshotRepository extends JpaRepository<AnnualizationSnapshot, Long> {
    List<AnnualizationSnapshot> findByTaxYearAndStatusOrderByEmployeeNameAsc(Integer taxYear, String status);
    Optional<AnnualizationSnapshot> findFirstByEmployeeNoIgnoreCaseAndTaxYearAndStatusOrderByVersionDesc(
            String employeeNo, Integer taxYear, String status);
    Optional<AnnualizationSnapshot> findFirstByEmployeeNoIgnoreCaseAndTaxYearOrderByVersionDesc(
            String employeeNo, Integer taxYear);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AnnualizationSnapshot s WHERE LOWER(s.employeeNo) = LOWER(:employeeNo) " +
            "AND s.taxYear = :taxYear AND s.status = 'FINALIZED'")
    Optional<AnnualizationSnapshot> findFinalizedForUpdate(
            @Param("employeeNo") String employeeNo, @Param("taxYear") Integer taxYear);
}
