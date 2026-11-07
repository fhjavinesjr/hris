package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationSnapshotPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnualizationSnapshotPeriodRepository extends JpaRepository<AnnualizationSnapshotPeriod, Long> {
    List<AnnualizationSnapshotPeriod> findBySnapshotIdOrderBySalaryDateAscSalaryPeriodKeyAsc(Long snapshotId);
    List<AnnualizationSnapshotPeriod> findBySnapshotIdInOrderBySnapshotIdAscSalaryDateAscSalaryPeriodKeyAsc(
            List<Long> snapshotIds);
}
