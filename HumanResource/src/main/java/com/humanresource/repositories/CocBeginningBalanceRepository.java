package com.humanresource.repositories;

import com.humanresource.entitymodels.CocBeginningBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CocBeginningBalanceRepository extends JpaRepository<CocBeginningBalance, Long> {
    Optional<CocBeginningBalance> findByEmployeeId(Long employeeId);
}
