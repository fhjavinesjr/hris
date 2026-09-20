package com.administrative.services;

import com.administrative.entitymodels.ManagePersonnel;
import com.administrative.entitymodels.BusinessUnits;
import com.administrative.repositories.BusinessUnitsRepository;
import com.administrative.repositories.ManagePersonnelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagePersonnelServiceTest {
    private ManagePersonnelRepository repository;
    private ManagePersonnelService service;

    @BeforeEach
    void setUp() {
        repository = mock(ManagePersonnelRepository.class);
        service = new ManagePersonnelService();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "businessUnitsRepository", mock(BusinessUnitsRepository.class));
        when(repository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsSecondRegularHeadInSameUnit() {
        when(repository.findAll()).thenReturn(new ArrayList<>(List.of(head(1L, 10L, null, null, null))));

        assertThatThrownBy(() -> service.saveAll(List.of(head(2L, 10L, null, null, null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only one regular Head");
    }

    @Test
    void rejectsOverlappingOicPeriods() {
        when(repository.findAll()).thenReturn(new ArrayList<>(List.of(
                head(2L, 10L, "OIC", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30)))));

        assertThatThrownBy(() -> service.saveAll(List.of(
                head(3L, 10L, "OIC", LocalDate.of(2026, 9, 15), null))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("may not overlap");
    }

    @Test
    void oicRequiresEffectiveFromAndHeadFlag() {
        ManagePersonnel invalid = head(2L, 10L, "OIC", null, null);
        invalid.setHead(false);
        when(repository.findAll()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> service.saveAll(List.of(invalid)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marked as Head");
    }

    @Test
    void activeOicExclusivelyReplacesRegularHeadForCoveredDates() {
        ManagePersonnel regular = head(1L, 10L, null, null, null);
        ManagePersonnel oic = head(2L, 10L, "OIC",
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));
        ManagePersonnel staff = new ManagePersonnel();
        staff.setEmployeeId(3L);
        staff.setBusinessUnitId(10L);
        staff.setAreaId(1L);
        staff.setBase("No");
        when(repository.findAll()).thenReturn(new ArrayList<>(List.of(regular, oic, staff)));
        BusinessUnits unit = mock(BusinessUnits.class);
        when(unit.getBusinessUnitsName()).thenReturn("HR");
        BusinessUnitsRepository units = (BusinessUnitsRepository) ReflectionTestUtils.getField(service, "businessUnitsRepository");
        when(units.findById(10L)).thenReturn(java.util.Optional.of(unit));

        assertThat(service.getSupervisedUnits(1L, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 15)))
                .isEmpty();
        assertThat(service.getSupervisedUnits(2L, LocalDate.of(2026, 9, 15), LocalDate.of(2026, 9, 15)))
                .singleElement()
                .satisfies(access -> {
                    org.assertj.core.api.Assertions.assertThat(access.role()).isEqualTo("OIC");
                    org.assertj.core.api.Assertions.assertThat(access.personnelEmployeeIds()).containsExactly(1L, 2L, 3L);
                });
    }

    private ManagePersonnel head(Long employeeId, Long unitId, String status,
                                 LocalDate from, LocalDate to) {
        ManagePersonnel value = new ManagePersonnel();
        value.setEmployeeId(employeeId);
        value.setBusinessUnitId(unitId);
        value.setAreaId(1L);
        value.setHead(true);
        value.setOtherStatus(status);
        value.setOicEffectiveFrom(from);
        value.setOicEffectiveTo(to);
        value.setBase("No");
        return value;
    }
}
