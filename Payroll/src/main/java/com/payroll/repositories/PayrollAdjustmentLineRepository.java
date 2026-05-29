package com.payroll.repositories;

import com.payroll.entitymodels.PayrollAdjustmentLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Repository
public interface PayrollAdjustmentLineRepository extends JpaRepository<PayrollAdjustmentLine, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM PayrollAdjustmentLine l WHERE l.header.id IN :headerIds")
    int deleteByHeaderIdIn(@Param("headerIds") Collection<Long> headerIds);
}
