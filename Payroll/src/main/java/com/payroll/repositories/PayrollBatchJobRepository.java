package com.payroll.repositories;

import com.payroll.entitymodels.PayrollBatchJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PayrollBatchJobRepository extends JpaRepository<PayrollBatchJob, Long> {

    Optional<PayrollBatchJob> findByJobId(String jobId);

    /**
     * Lightweight progress update — avoids loading and re-saving the full entity
     * from a background thread (prevents optimistic-locking conflicts).
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE PayrollBatchJob j
            SET j.processedEmployees = :processed,
                j.failedEmployees    = :failed,
                j.progressPct        = :pct
            WHERE j.jobId = :jobId
            """)
    void updateProgress(@Param("jobId")    String jobId,
                        @Param("processed") int processed,
                        @Param("failed")    int failed,
                        @Param("pct")       int pct);
}
