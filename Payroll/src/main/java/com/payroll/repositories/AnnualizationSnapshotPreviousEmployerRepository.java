package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationSnapshotPreviousEmployer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnualizationSnapshotPreviousEmployerRepository extends JpaRepository<AnnualizationSnapshotPreviousEmployer, Long> {
    List<AnnualizationSnapshotPreviousEmployer> findBySnapshotIdOrderByEmploymentFromAsc(Long snapshotId);
}
