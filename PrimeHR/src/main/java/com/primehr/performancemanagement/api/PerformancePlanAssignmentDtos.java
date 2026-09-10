package com.primehr.performancemanagement.api;

import com.primehr.performancemanagement.domain.PerformancePlanAssignment;
import jakarta.validation.constraints.*;
import java.util.List;

public final class PerformancePlanAssignmentDtos {
    private PerformancePlanAssignmentDtos() {}
    public record AssignmentInput(@NotBlank @Size(max=36) String cycleId,
        @NotBlank @Size(max=36) String templateVersionId,@NotNull PerformancePlanAssignment.SubjectType subjectType,
        @NotNull @Positive Long subjectId,@Positive Long routingBusinessUnitId,@NotNull @Positive Long ownerEmployeeId,
        @NotEmpty List<@NotBlank @Size(max=36) String> objectiveVersionIds,Long recordVersion) {}
    public record Transition(@NotNull Long recordVersion,@NotBlank @Size(max=1000) String reason) {}
    public record AssignmentResponse(String id,String cycleId,String templateVersionId,String subjectType,Long subjectId,
        String subjectCode,String subjectName,Long areaId,String areaName,Long routingBusinessUnitId,
        String routingBusinessUnitName,Long ownerEmployeeId,String ownerEmployeeNo,String ownerName,
        Long ownerAppointmentId,Long ownerJobPositionId,Long ownerPlantillaId,String formType,
        String organizationFingerprint,String participantFingerprint,String routeFingerprint,String status,
        long recordVersion,List<String> objectiveVersionIds) {}
    public record ReadinessResponse(boolean ready,List<String> errors) {}
}
