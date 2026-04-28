package com.humanresource.repositories;

import com.humanresource.entitymodels.PassSlip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PassSlipRepository extends JpaRepository<PassSlip, Long> {

    List<PassSlip> findByEmployeeId(Long employeeId);

    List<PassSlip> findByEmployeeIdAndStatus(Long employeeId, String status);

    List<PassSlip> findByEmployeeIdAndDateFiledBetween(Long employeeId, LocalDate from, LocalDate to);

    List<PassSlip> findByEmployeeIdAndPassSlipDateBetween(Long employeeId, LocalDate from, LocalDate to);

    List<PassSlip> findByStatusOrderByDateFiledDesc(String status);
}
