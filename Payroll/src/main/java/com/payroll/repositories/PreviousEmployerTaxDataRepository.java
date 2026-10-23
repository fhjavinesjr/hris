package com.payroll.repositories;

import com.payroll.entitymodels.PreviousEmployerTaxData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreviousEmployerTaxDataRepository extends JpaRepository<PreviousEmployerTaxData, Long> {
    List<PreviousEmployerTaxData> findByTaxYearOrderByEmployeeNoAscEmploymentFromAsc(Integer taxYear);
    List<PreviousEmployerTaxData> findByEmployeeNoIgnoreCaseAndTaxYearOrderByEmploymentFromAsc(
            String employeeNo, Integer taxYear);
    Optional<PreviousEmployerTaxData> findByIdAndEmployeeNoIgnoreCase(Long id, String employeeNo);
}
