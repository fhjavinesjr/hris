package com.payroll.repositories;

import com.payroll.entitymodels.AnnualizationRefundVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnualizationRefundVoucherRepository extends JpaRepository<AnnualizationRefundVoucher, Long> {
    Optional<AnnualizationRefundVoucher> findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(Long snapshotId);
}
