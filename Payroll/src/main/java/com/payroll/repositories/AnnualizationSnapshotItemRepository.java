package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationSnapshotItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnualizationSnapshotItemRepository
        extends JpaRepository<AnnualizationSnapshotItem, Long> {
    List<AnnualizationSnapshotItem> findBySnapshotIdOrderByItemNoAsc(Long snapshotId);
}
