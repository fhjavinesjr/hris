package com.administrative.repositories;

import com.administrative.entitymodels.PagIbigContribution;
import com.administrative.entitymodels.PayrollSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class LatestConfigurationRepositoryTest {

    @Autowired
    private PayrollSettingsRepository payrollSettingsRepository;

    @Autowired
    private PagIbigContributionRepository pagIbigContributionRepository;

    @Test
    void findsPayrollSettingsWithLatestEffectivityDate() {
        payrollSettingsRepository.save(new PayrollSettings(
                null,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                22,
                22,
                false
        ));
        PayrollSettings expected = payrollSettingsRepository.save(new PayrollSettings(
                null,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                20,
                20,
                true
        ));

        assertThat(payrollSettingsRepository.findFirstByOrderByEffectivityDateDesc())
                .contains(expected);
    }

    @Test
    void findsPagIbigContributionWithLatestEffectivityDate() {
        pagIbigContributionRepository.save(new PagIbigContribution(
                null,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                100.0,
                100.0
        ));
        PagIbigContribution expected = pagIbigContributionRepository.save(new PagIbigContribution(
                null,
                LocalDateTime.of(2025, 1, 1, 0, 0),
                200.0,
                200.0
        ));

        assertThat(pagIbigContributionRepository.findFirstByOrderByEffectivityDateDesc())
                .contains(expected);
    }
}
