package com.humanresource.saln;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface SalnRepository extends JpaRepository<Saln,Long> {
    List<Saln> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    @Query("select s from Saln s where s.id=:id") Optional<Saln> findDetailedById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from Saln s where s.id=:id") Optional<Saln> findByIdForUpdate(@Param("id")Long id);
    boolean existsByEmployeeIdAndFilingTypeAndSalnYearAndAsOfDateAndStatusNot(Long employeeId,SalnTypes.FilingType filingType,Integer salnYear,java.time.LocalDate asOfDate,SalnTypes.Status status);
    Optional<Saln> findFirstByEmployeeIdAndFilingTypeAndReferenceDateAndStatusNotOrderByUpdatedAtDesc(Long employeeId,SalnTypes.FilingType filingType,java.time.LocalDate referenceDate,SalnTypes.Status status);
}
