package com.administrative.impl;

import com.administrative.dtos.PerformancePlanningSourceDtos.OrganizationType;
import com.administrative.entitymodels.*;
import com.administrative.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PerformancePlanningSourceServiceImplTest {
    private final AreasRepository areas = mock(AreasRepository.class);
    private final BusinessUnitsRepository units = mock(BusinessUnitsRepository.class);
    private final ManagePersonnelRepository personnel = mock(ManagePersonnelRepository.class);
    private final EmployeeRequestRepository requests = mock(EmployeeRequestRepository.class);
    private final ApprovalWorkflowRepository workflows = mock(ApprovalWorkflowRepository.class);
    private final PerformancePlanningSourceServiceImpl service = new PerformancePlanningSourceServiceImpl(
            areas, units, personnel, requests, workflows);

    @Test
    void returnsMinimalFingerprintableOrganizationAndPersonnelSnapshots() {
        Areas area = new Areas(4L, "Medical Services", null);
        BusinessUnits unit = new BusinessUnits(8L, "WARD", "Ward Services", area);
        ManagePersonnel membership = new ManagePersonnel();
        membership.setEmployeeId(12L);
        membership.setAreaId(4L);
        membership.setBusinessUnitId(8L);
        membership.setHead(true);
        membership.setBase("Main");
        when(units.findById(8L)).thenReturn(Optional.of(unit));
        when(areas.findById(4L)).thenReturn(Optional.of(area));
        when(personnel.findByEmployeeIdOrderById(12L)).thenReturn(List.of(membership));

        var organization = service.organization(OrganizationType.BUSINESS_UNIT, 8L);
        var result = service.membership(12L);

        assertThat(organization.areaId()).isEqualTo(4L);
        assertThat(organization.sourceFingerprint()).hasSize(64);
        assertThat(result.businessUnitCode()).isEqualTo("WARD");
        assertThat(result.head()).isTrue();
        assertThat(result.sourceFingerprint()).hasSize(64);
    }

    @Test
    void failsClosedForDuplicateOrInconsistentPersonnelMembership() {
        ManagePersonnel first = new ManagePersonnel();
        ManagePersonnel second = new ManagePersonnel();
        when(personnel.findByEmployeeIdOrderById(12L)).thenReturn(List.of(first, second));

        assertThatThrownBy(() -> service.membership(12L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));

        Areas expectedArea = new Areas(4L, "Expected", null);
        Areas otherArea = new Areas(5L, "Other", null);
        BusinessUnits unit = new BusinessUnits(8L, "WARD", "Ward Services", otherArea);
        first.setEmployeeId(12L);
        first.setAreaId(4L);
        first.setBusinessUnitId(8L);
        when(personnel.findByEmployeeIdOrderById(12L)).thenReturn(List.of(first));
        when(units.findById(8L)).thenReturn(Optional.of(unit));
        when(areas.findById(4L)).thenReturn(Optional.of(expectedArea));

        assertThatThrownBy(() -> service.membership(12L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void rejectsUnsupportedOrGappedApprovalRoutes() {
        assertThatThrownBy(() -> service.approvalRoute(8L, "LEAVE"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Unsupported");

        Areas area = new Areas(4L, "Medical Services", null);
        BusinessUnits unit = new BusinessUnits(8L, "WARD", "Ward Services", area);
        EmployeeRequest request = new EmployeeRequest(3L, "PERFORMANCE_OFFICE_COMMITMENT", "OPCR", 2);
        ApprovalWorkflow step = new ApprovalWorkflow();
        step.setApprovalWorkflowId(9L);
        step.setApprovalLevel(2);
        step.setEmployeeId(12L);
        step.setAreaId(4L);
        when(units.findById(8L)).thenReturn(Optional.of(unit));
        when(requests.findByCodeIgnoreCase("PERFORMANCE_OFFICE_COMMITMENT")).thenReturn(Optional.of(request));
        when(workflows.findByBusinessUnitIdAndEmployeeRequestIdOrderByApprovalLevelAscApprovalWorkflowIdAsc(8L, 3L))
                .thenReturn(List.of(step));

        assertThatThrownBy(() -> service.approvalRoute(8L, "PERFORMANCE_OFFICE_COMMITMENT"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(units, times(1)).findById(any());
    }
}
