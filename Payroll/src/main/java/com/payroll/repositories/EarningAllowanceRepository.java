package com.payroll.repositories;

import com.payroll.entitymodels.EarningAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EarningAllowanceRepository extends JpaRepository<EarningAllowance, Long> {
}
