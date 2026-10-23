package com.payroll.repositories;

import com.payroll.entitymodels.PayrollStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class AnnualizationRepositoryQueryTest {

    @Autowired private PayrollDetailRepository detailRepository;
    @Autowired private PayrollDetailEarningRepository earningRepository;
    @Autowired private PayrollDetailDeductionRepository deductionRepository;
    @Autowired private PayrollAdjustmentLineRepository adjustmentLineRepository;
    @Autowired private AnnualizationRefundVoucherRepository refundVoucherRepository;

    @Test
    void annualizationQueriesAreValidAndReturnEmptyCollectionsWithoutData() {
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);

        assertTrue(detailRepository.findFinalizedForAnnualization(
                from, to, PayrollStatus.CANCELLED).isEmpty());
        assertTrue(earningRepository.findFinalizedForAnnualization(
                from, to, PayrollStatus.CANCELLED).isEmpty());
        assertTrue(deductionRepository.findFinalizedForAnnualization(
                from, to, PayrollStatus.CANCELLED).isEmpty());
        assertTrue(adjustmentLineRepository.findPostedForAnnualization(
                from, to, PayrollStatus.CANCELLED).isEmpty());
        assertTrue(refundVoucherRepository
                .findFirstBySnapshotIdAndVoidedAtIsNullOrderByIdDesc(1L).isEmpty());
    }
}
